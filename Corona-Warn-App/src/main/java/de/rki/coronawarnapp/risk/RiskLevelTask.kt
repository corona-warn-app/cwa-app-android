package de.rki.coronawarnapp.risk

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindowCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.sortDateTime
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.risk.EwRiskLevelResult.FailureReason
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.task.common.Finished
import de.rki.coronawarnapp.task.common.Started
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeZone.UTC
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@Suppress("ReturnCount", "LongParameterList")
class RiskLevelTask @Inject constructor(
    private val riskLevels: RiskLevels,
    private val enfClient: ENFClient,
    private val timeStamper: TimeStamper,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val riskLevelSettings: RiskLevelSettings,
    private val appConfigProvider: AppConfigProvider,
    private val riskLevelStorage: RiskLevelStorage,
    private val keyCacheRepository: KeyCacheRepository,
    private val coronaTestRepository: CoronaTestRepository,
    private val analyticsExposureWindowCollector: AnalyticsExposureWindowCollector,
    private val analyticsTestResultCollector: AnalyticsTestResultCollector
) : Task<DefaultProgress, EwRiskLevelTaskResult> {

    private val internalProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = internalProgress

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): EwRiskLevelTaskResult = try {
        Timber.d("Running with arguments=%s", arguments)

        val configData: ConfigData = appConfigProvider.getAppConfig()

        determineRiskLevelResult(configData).also {
            Timber.i("Risk level determined: %s", it)

            checkCancel()

            Timber.tag(TAG).d("storeTaskResult(...)")
            riskLevelStorage.storeResult(it)

            riskLevelSettings.lastUsedConfigIdentifier = configData.identifier
        }
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        error.report(ExceptionCategory.EXPOSURENOTIFICATION)
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.value = Finished
    }

    private suspend fun determineRiskLevelResult(configData: ConfigData): EwRiskLevelTaskResult {
        val nowUtc = timeStamper.nowUTC.also {
            Timber.d("The current time is %s", it)
        }

        val isAllowedToSubmitKeys = coronaTestRepository.coronaTests.first().any { it.isSubmissionAllowed }
        val hasViewedTestResult = coronaTestRepository.coronaTests.first().any { it.isViewed }
        if (isAllowedToSubmitKeys && hasViewedTestResult) {
            Timber.i("Positive test result and user has seen it, skip risk calculation")
            return EwRiskLevelTaskResult(
                calculatedAt = nowUtc,
                failureReason = FailureReason.POSITIVE_TEST_RESULT
            )
        }

        if (!configData.isDeviceTimeCorrect) {
            Timber.w("Device time is incorrect, offset: %s", configData.localOffset)
            val currentServerTime = nowUtc.minus(configData.localOffset)
            Timber.d("Calculated current server time: %s", currentServerTime)
            return EwRiskLevelTaskResult(
                calculatedAt = currentServerTime,
                failureReason = FailureReason.INCORRECT_DEVICE_TIME
            )
        }

        if (!enfClient.isTracingEnabled.first()) {
            Timber.i("Risk not calculated, tracing is disabled.")
            return EwRiskLevelTaskResult(
                calculatedAt = nowUtc,
                failureReason = FailureReason.TRACING_OFF
            )
        }

        if (areKeyPkgsOutDated(nowUtc)) {
            Timber.i("Risk not calculated, results are outdated.")
            return EwRiskLevelTaskResult(
                calculatedAt = nowUtc,
                failureReason = when (backgroundJobsEnabled()) {
                    true -> FailureReason.OUTDATED_RESULTS
                    false -> FailureReason.OUTDATED_RESULTS_MANUAL
                }
            )
        }
        checkCancel()

        return calculateRiskLevel(configData, nowUtc)
    }

    @VisibleForTesting
    internal suspend fun areKeyPkgsOutDated(nowUTC: Instant): Boolean {
        Timber.tag(TAG).d("Evaluating areKeyPkgsOutDated(nowUTC=%s)", nowUTC)

        val latestDownload = keyCacheRepository.getAllCachedKeys().maxByOrNull {
            it.info.sortDateTime
        }
        if (latestDownload == null) {
            Timber.w("areKeyPkgsOutDated(): No downloads available, why is the RiskLevelTask running? Aborting!")
            return true
        }

        val downloadAge = Duration(latestDownload.info.sortDateTime, nowUTC).also {
            Timber.d("areKeyPkgsOutDated(): Age is %dh for latest key package: %s", it.standardHours, latestDownload)
        }

        return (downloadAge.isLongerThan(STALE_DOWNLOAD_LIMIT)).also {
            if (it) {
                Timber.tag(TAG).i("areKeyPkgsOutDated(): Calculation was not possible because results are outdated.")
            } else {
                Timber.tag(TAG).d("areKeyPkgsOutDated(): Key pkgs are fresh :), continuing evaluation.")
            }
        }
    }

    private suspend fun calculateRiskLevel(
        configData: ExposureWindowRiskCalculationConfig,
        nowUtc: Instant
    ): EwRiskLevelTaskResult {
        Timber.tag(TAG).d("Calculating risklevel")
        val exposureWindows = enfClient.exposureWindows().filterByAge(
            maxAgeInDays = configData.maxEncounterAgeInDays,
            nowUtc = nowUtc
        )

        return determineRisk(configData, exposureWindows).let {
            Timber.tag(TAG).d("Risklevel calculated: %s", it)
            if (it.isIncreasedRisk()) {
                Timber.tag(TAG).i("Risk is increased!")
            } else {
                Timber.tag(TAG).d("Risk is not increased, continuing evaluating.")
            }

            EwRiskLevelTaskResult(
                calculatedAt = timeStamper.nowUTC,
                ewAggregatedRiskResult = it,
                exposureWindows = exposureWindows
            )
        }
    }

    private suspend fun determineRisk(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindows: List<ExposureWindow>
    ): EwAggregatedRiskResult {
        val riskResultsPerWindow =
            exposureWindows.mapNotNull { window ->
                riskLevels.calculateRisk(appConfig, window)?.let { window to it }
            }.toMap()

        analyticsExposureWindowCollector.reportRiskResultsPerWindow(riskResultsPerWindow)
        analyticsTestResultCollector.reportRiskResultsPerWindow(riskResultsPerWindow)

        return riskLevels.aggregateResults(appConfig, riskResultsPerWindow)
    }

    private suspend fun backgroundJobsEnabled() =
        backgroundModeStatus.isAutoModeEnabled.first().also {
            if (it) {
                Timber.tag(TAG).v("diagnosis keys outdated and active tracing time is above threshold")
                Timber.tag(TAG).v("manual mode not active (background jobs enabled)")
            } else {
                Timber.tag(TAG).v("diagnosis keys outdated and active tracing time is above threshold")
                Timber.tag(TAG).v("manual mode active (background jobs disabled)")
            }
        }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Config(
        private val exposureDetectionTracker: ExposureDetectionTracker,
        override val executionTimeout: Duration = Duration.standardMinutes(8),
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    ) : TaskFactory.Config {

        override val preconditions: List<suspend () -> Boolean>
            get() = listOf {
                // check whether we already have a successful v2 exposure
                exposureDetectionTracker.calculations.first().values.any {
                    it.enfVersion == TrackedExposureDetection.EnfVersion.V2_WINDOW_MODE && it.isSuccessful
                }
            }
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<RiskLevelTask>,
        private val exposureDetectionTracker: ExposureDetectionTracker
    ) : TaskFactory<DefaultProgress, EwRiskLevelTaskResult> {

        override suspend fun createConfig(): TaskFactory.Config = Config(exposureDetectionTracker)
        override val taskProvider: () -> Task<DefaultProgress, EwRiskLevelTaskResult> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG = tag<RiskLevelTask>()
        private val STALE_DOWNLOAD_LIMIT = Duration.standardHours(48)
    }
}

@VisibleForTesting
internal fun List<ExposureWindow>.filterByAge(
    maxAgeInDays: Int,
    nowUtc: Instant
): List<ExposureWindow> {
    val deadline = nowUtc.minusDays(maxAgeInDays).millis
    return filter {
        it.dateMillisSinceEpoch >= deadline
    }
}

private fun Instant.minusDays(days: Int) = toLocalDateUtc().minusDays(days).toDateTimeAtStartOfDay(UTC)
