package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.google.gson.Gson
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.risk.ExposureResult
import de.rki.coronawarnapp.risk.ExposureResultStore
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.risk.RiskLevels
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.SecurityHelper
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TestRiskLevelCalculationFragmentCWAViewModel @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val exampleArg: String?,
    @AppContext private val context: Context, // App context
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    private val riskLevels: RiskLevels,
    private val taskController: TaskController,
    private val keyCacheRepository: KeyCacheRepository,
    private val appConfigProvider: AppConfigProvider,
    tracingCardStateProvider: TracingCardStateProvider,
    @BaseGson private val gson: Gson,
    private val exposureResultStore: ExposureResultStore
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val startLocalQRCodeScanEvent = SingleLiveEvent<Unit>()
    val riskLevelResetEvent = SingleLiveEvent<Unit>()
    val apiKeysProvidedEvent = SingleLiveEvent<DiagnosisKeyProvidedEvent>()
    val riskScoreState = MutableLiveData<RiskScoreState>(RiskScoreState())
    val showRiskStatusCard = SubmissionRepository.deviceUIStateFlow.map {
        it.withSuccess(false) { true }
    }.asLiveData(dispatcherProvider.Default)

    val tracingCardState = tracingCardStateProvider.state
        .sample(150L)
        .asLiveData(dispatcherProvider.Default)

    init {
        Timber.d("CWAViewModel: %s", this)
        Timber.d("SavedStateHandle: %s", handle)
        Timber.d("Example arg: %s", exampleArg)
    }

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

                    exposureResultStore.entities.value = ExposureResult(emptyList(), null)

                    LocalData.lastCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                    LocalData.lastSuccessfullyCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                    LocalData.lastTimeDiagnosisKeysFromServerFetch(null)
                } catch (e: Exception) {
                    e.report(ExceptionCategory.INTERNAL)
                }
            }
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            riskLevelResetEvent.postValue(Unit)
        }
    }

    data class RiskScoreState(
        val riskScoreMsg: String = "",
        val backendParameters: String = "",
        val formula: String = "",
    )

    fun startENFObserver() {
        launch {
            try {
                val appConfig = appConfigProvider.getAppConfig()

                var workState = riskScoreState.value!!

                val riskAsString = "Level: ${RiskLevelRepository.getLastCalculatedScore()}\n" +
                    "Last successful Level: " +
                    "${LocalData.lastSuccessfullyCalculatedRiskLevel()}\n" +
                    "Last Time Server Fetch: ${LocalData.lastTimeDiagnosisKeysFromServerFetch()}\n" +
                    "Tracing Duration: " +
                    "${TimeUnit.MILLISECONDS.toDays(TimeVariables.getTimeActiveTracingDuration())} days \n" +
                    "Tracing Duration in last 14 days: " +
                    "${TimeVariables.getActiveTracingDaysInRetentionPeriod()} days \n" +
                    "Last time risk level calculation ${LocalData.lastTimeRiskLevelCalculation()}"

                workState = workState.copy(riskScoreMsg = riskAsString)

                val configAsString =
                    "Transmission RiskLevel Multiplier: ${appConfig.transmissionRiskLevelMultiplier}\n" +
                        "Minutes At Attenuation Filters: ${appConfig.minutesAtAttenuationFilters}\n" +
                        "Minutes At Attenuation Weights: ${appConfig.minutesAtAttenuationWeights}" +
                        "Transmission RiskLevel Encoding: ${appConfig.transmissionRiskLevelEncoding}" +
                        "Transmission RiskLevel Filters: ${appConfig.transmissionRiskLevelFilters}" +
                        "Normalized Time Per Exposure Window To RiskLevel Mapping: ${appConfig.normalizedTimePerExposureWindowToRiskLevelMapping}" +
                        "Normalized Time Per Day To RiskLevel Mapping List: ${appConfig.normalizedTimePerDayToRiskLevelMappingList}"
                workState = workState.copy(backendParameters = configAsString)

                riskScoreState.postValue(workState)
            } catch (e: Exception) {
                e.report(ExceptionCategory.EXPOSURENOTIFICATION)
            }
        }
    }


    val exposureWindowCountString = exposureResultStore
        .entities
        .map { "Retrieved ${it.exposureWindows.size} Exposure Windows" }
        .asLiveData()

    val exposureWindows = exposureResultStore
        .entities
        .map { if (it.exposureWindows.isEmpty()) "Exposure windows list is empty" else gson.toJson(it.exposureWindows) }
        .asLiveData()

    val aggregatedRiskResult = exposureResultStore
        .entities
        .map { if (it.aggregatedRiskResult != null)  it.aggregatedRiskResult.toReadableString() else "Aggregated risk result is not available" }
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

    data class DiagnosisKeyProvidedEvent(
        val keyCount: Int
    )

    fun clearKeyCache() {
        Timber.d("Clearing key cache")
        launch { keyCacheRepository.clear() }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<TestRiskLevelCalculationFragmentCWAViewModel> {
        fun create(
            handle: SavedStateHandle,
            exampleArg: String?
        ): TestRiskLevelCalculationFragmentCWAViewModel
    }
}
