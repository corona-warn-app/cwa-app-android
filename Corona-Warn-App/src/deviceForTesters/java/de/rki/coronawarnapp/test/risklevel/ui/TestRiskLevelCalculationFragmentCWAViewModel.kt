package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.test.risklevel.entities.toExposureWindowJson
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.ui.tracing.common.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.security.SecurityHelper
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit

class TestRiskLevelCalculationFragmentCWAViewModel @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val exampleArg: String?,
    @AppContext private val context: Context, // App context
    private val dispatcherProvider: DispatcherProvider,
    private val taskController: TaskController,
    private val keyCacheRepository: KeyCacheRepository,
    private val appConfigProvider: AppConfigProvider,
    tracingCardStateProvider: TracingCardStateProvider,
    private val riskLevelStorage: RiskLevelStorage,
    private val testSettings: TestSettings
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    // Use unique instance for pretty output
    private val gson: Gson by lazy {
        GsonBuilder().setPrettyPrinting().create()
    }

    val fakeWindowsState = testSettings.fakeExposureWindows.flow.asLiveData()

    init {
        Timber.d("CWAViewModel: %s", this)
        Timber.d("SavedStateHandle: %s", handle)
        Timber.d("Example arg: %s", exampleArg)
    }

    val riskLevelResetEvent = SingleLiveEvent<Unit>()
    val shareFileEvent = SingleLiveEvent<File>()

    val showRiskStatusCard = SubmissionRepository.deviceUIStateFlow.map {
        it.withSuccess(false) { true }
    }.asLiveData(dispatcherProvider.Default)

    val tracingCardState = tracingCardStateProvider.state
        .sample(150L)
        .asLiveData(dispatcherProvider.Default)

    val exposureWindowCount = riskLevelStorage
        .exposureWindows
        .map { it.size }
        .asLiveData()

    val aggregatedRiskResult = riskLevelStorage
        .riskLevelResults
        .map {
            val latest = it.maxByOrNull { it.calculatedAt }
            if (latest?.aggregatedRiskResult != null) {
                latest.aggregatedRiskResult?.toReadableString()
            } else {
                "Aggregated risk result is not available"
            }
        }
        .asLiveData()

    private fun AggregatedRiskResult.toReadableString(): String = StringBuilder()
        .appendLine("Total RiskLevel: $totalRiskLevel")
        .appendLine("Total Minimum Distinct Encounters With High Risk: $totalMinimumDistinctEncountersWithHighRisk")
        .appendLine("Total Minimum Distinct Encounters With Low Risk: $totalMinimumDistinctEncountersWithLowRisk")
        .appendLine("Most Recent Date With High Risk: $mostRecentDateWithHighRisk")
        .appendLine("Most Recent Date With Low Risk: $mostRecentDateWithLowRisk")
        .appendLine("Number of Days With High Risk: $numberOfDaysWithHighRisk")
        .appendLine("Number of Days With Low Risk: $numberOfDaysWithLowRisk")
        .toString()

    val backendParameters = appConfigProvider
        .currentConfig
        .map { it.toReadableString() }
        .asLiveData()

    private fun ConfigData.toReadableString(): String = StringBuilder()
        .appendLine("Transmission RiskLevel Multiplier: $transmissionRiskLevelMultiplier")
        .appendLine()
        .appendLine("Minutes At Attenuation Filters:")
        .appendLine(minutesAtAttenuationFilters)
        .appendLine()
        .appendLine("Minutes At Attenuation Weights:")
        .appendLine(minutesAtAttenuationWeights)
        .appendLine()
        .appendLine("Transmission RiskLevel Encoding:")
        .appendLine(transmissionRiskLevelEncoding)
        .appendLine()
        .appendLine("Transmission RiskLevel Filters:")
        .appendLine(transmissionRiskLevelFilters)
        .appendLine()
        .appendLine("Normalized Time Per Exposure Window To RiskLevel Mapping:")
        .appendLine(normalizedTimePerExposureWindowToRiskLevelMapping)
        .appendLine()
        .appendLine("Normalized Time Per Day To RiskLevel Mapping List:")
        .appendLine(normalizedTimePerDayToRiskLevelMappingList)
        .toString()

    val additionalRiskCalcInfo = combine(
        riskLevelStorage.riskLevelResults,
        LocalData.lastTimeDiagnosisKeysFromServerFetchFlow()
    ) { riskLevelResults, lastTimeDiagnosisKeysFromServerFetch ->

        val (latestCalc, latestSuccessfulCalc) = riskLevelResults.tryLatestResultsWithDefaults()

        createAdditionalRiskCalcInfo(
            latestCalc.calculatedAt,
            riskLevelScore = latestCalc.riskLevel.raw,
            riskLevelScoreLastSuccessfulCalculated = latestSuccessfulCalc.riskLevel.raw,
            matchedKeyCount = latestCalc.matchedKeyCount,
            daysSinceLastExposure = latestCalc.daysWithEncounters,
            lastTimeDiagnosisKeysFromServerFetch = lastTimeDiagnosisKeysFromServerFetch
        )
    }.asLiveData()

    private suspend fun createAdditionalRiskCalcInfo(
        lastTimeRiskLevelCalculation: Instant,
        riskLevelScore: Int,
        riskLevelScoreLastSuccessfulCalculated: Int,
        matchedKeyCount: Int,
        daysSinceLastExposure: Int,
        lastTimeDiagnosisKeysFromServerFetch: Date?
    ): String = StringBuilder()
        .appendLine("Risk Level: ${RiskLevel.forValue(riskLevelScore)}")
        .appendLine("Last successful Risk Level: ${RiskLevel.forValue(riskLevelScoreLastSuccessfulCalculated)}")
        .appendLine("Matched key count: $matchedKeyCount")
        .appendLine("Days since last Exposure: $daysSinceLastExposure days")
        .appendLine("Last Time Server Fetch: ${lastTimeDiagnosisKeysFromServerFetch?.time?.let { Instant.ofEpochMilli(it) }}")
        .appendLine("Tracing Duration: ${TimeUnit.MILLISECONDS.toDays(TimeVariables.getTimeActiveTracingDuration())} days")
        .appendLine("Tracing Duration in last 14 days: ${TimeVariables.getActiveTracingDaysInRetentionPeriod()} days")
        .appendLine("Last time risk level calculation $lastTimeRiskLevelCalculation")
        .toString()

    fun retrieveDiagnosisKeys() {
        Timber.d("Starting download diagnosis keys task")
        launch {
            taskController.submitBlocking(
                DefaultTaskRequest(
                    DownloadDiagnosisKeysTask::class,
                    DownloadDiagnosisKeysTask.Arguments(),
                    originTag = "TestRiskLevelCalculationFragmentCWAViewModel.retrieveDiagnosisKeys()"
                )
            )
        }
    }

    fun calculateRiskLevel() {
        Timber.d("Starting calculate risk task")
        taskController.submit(
            DefaultTaskRequest(
                RiskLevelTask::class,
                originTag = "TestRiskLevelCalculationFragmentCWAViewModel.calculateRiskLevel()"
            )
        )
    }

    fun resetRiskLevel() {
        Timber.d("Resetting risk level")
        launch {
            withContext(Dispatchers.IO) {
                try {
                    // Preference reset
                    SecurityHelper.resetSharedPrefs()
                    // Database Reset
                    AppDatabase.reset(context)
                    // Export File Reset
                    keyCacheRepository.clear()

                    riskLevelStorage.clear()

                    LocalData.lastTimeDiagnosisKeysFromServerFetch(null)
                } catch (e: Exception) {
                    e.report(ExceptionCategory.INTERNAL)
                }
            }
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            riskLevelResetEvent.postValue(Unit)
        }
    }

    fun shareExposureWindows() {
        Timber.d("Creating text file for Exposure Windows")
        launch(dispatcherProvider.IO) {
            val exposureWindows = riskLevelStorage.exposureWindows.firstOrNull()

            val path = File(context.cacheDir, "share/")
            path.mkdirs()

            val file = File(path, "exposureWindows.txt")
            file.bufferedWriter()
                .use {
                    if (exposureWindows.isNullOrEmpty()) {
                        it.appendLine("Exposure windows list was empty")
                    } else {
                        Timber.d("Exposure count: ${exposureWindows.size}")
                        it.appendLine(gson.toJson(exposureWindows))
                    }
                }
            shareFileEvent.postValue(file)
        }
    }

    fun clearKeyCache() {
        Timber.d("Clearing key cache")
        launch { keyCacheRepository.clear() }
    }

    fun selectFakeExposureWindowMode(newMode: TestSettings.FakeExposureWindowTypes) {
        testSettings.fakeExposureWindows.update { newMode }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<TestRiskLevelCalculationFragmentCWAViewModel> {
        fun create(
            handle: SavedStateHandle,
            exampleArg: String?
        ): TestRiskLevelCalculationFragmentCWAViewModel
    }
}
