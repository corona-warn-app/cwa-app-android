package de.rki.coronawarnapp.risk

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.risk.RiskLevelResult.FailureReason
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.ConnectivityHelper.isNetworkEnabled
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@Suppress("ReturnCount")
class RiskLevelTask @Inject constructor(
    private val riskLevels: RiskLevels,
    @AppContext private val context: Context,
    private val enfClient: ENFClient,
    private val timeStamper: TimeStamper,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val riskLevelSettings: RiskLevelSettings,
    private val appConfigProvider: AppConfigProvider,
    private val riskLevelStorage: RiskLevelStorage,
    private val keyCacheRepository: KeyCacheRepository
) : Task<DefaultProgress, RiskLevelTaskResult> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): RiskLevelTaskResult = try {
        Timber.d("Running with arguments=%s", arguments)

        val configData: ConfigData = appConfigProvider.getAppConfig()

        determineRiskLevelResult(configData).also {
            Timber.i("Risklevel determined: %s", it)

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
        internalProgress.close()
    }

    private suspend fun determineRiskLevelResult(configData: ConfigData): RiskLevelTaskResult {
        val nowUTC = timeStamper.nowUTC.also {
            Timber.d("The current time is %s", it)
        }

        if (!configData.isDeviceTimeCorrect) {
            Timber.w("Device time is incorrect, offset: %s", configData.localOffset)
            return RiskLevelTaskResult(
                calculatedAt = nowUTC,
                failureReason = FailureReason.INCORRECT_DEVICE_TIME
            )
        }

        if (!isNetworkEnabled(context)) {
            Timber.i("Risk not calculated, internet unavailable.")
            return RiskLevelTaskResult(
                calculatedAt = nowUTC,
                failureReason = FailureReason.NO_INTERNET
            )
        }

        if (!enfClient.isTracingEnabled.first()) {
            Timber.i("Risk not calculated, tracing is disabled.")
            return RiskLevelTaskResult(
                calculatedAt = nowUTC,
                failureReason = FailureReason.TRACING_OFF
            )
        }

        if (areKeyPkgsOutDated(nowUTC)) {
            Timber.i("Risk not calculated, results are outdated.")
            return RiskLevelTaskResult(
                calculatedAt = nowUTC,
                failureReason = when (backgroundJobsEnabled()) {
                    true -> FailureReason.OUTDATED_RESULTS
                    false -> FailureReason.OUTDATED_RESULTS_MANUAL
                }
            )
        }
        checkCancel()

        return calculateRiskLevel(configData)
    }

    private suspend fun areKeyPkgsOutDated(nowUTC: Instant): Boolean {
        Timber.tag(TAG).d("Evaluating areKeyPkgsOutDated(nowUTC=%s)", nowUTC)

        val latestDownload = keyCacheRepository.getAllCachedKeys().maxByOrNull {
            it.info.toDateTime()
        }
        if (latestDownload == null) {
            Timber.w("areKeyPkgsOutDated(): No downloads available, why is the RiskLevelTask running? Aborting!")
            return true
        }

        val downloadAge = Duration(latestDownload.info.toDateTime(), nowUTC).also {
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

    private suspend fun calculateRiskLevel(configData: ExposureWindowRiskCalculationConfig): RiskLevelTaskResult {
        Timber.tag(TAG).d("Calculating risklevel")
        val exposureWindows = enfClient.exposureWindows()

        return riskLevels.determineRisk(configData, exposureWindows).let {
            Timber.tag(TAG).d("Risklevel calculated: %s", it)
            if (it.isIncreasedRisk()) {
                Timber.tag(TAG).i("Risk is increased!")
            } else {
                Timber.tag(TAG).d("Risk is not increased, continuing evaluating.")
            }

            RiskLevelTaskResult(
                calculatedAt = timeStamper.nowUTC,
                aggregatedRiskResult = it,
                exposureWindows = exposureWindows
            )
        }
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
    ) : TaskFactory<DefaultProgress, RiskLevelTaskResult> {

        override suspend fun createConfig(): TaskFactory.Config = Config(exposureDetectionTracker)
        override val taskProvider: () -> Task<DefaultProgress, RiskLevelTaskResult> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG: String? = RiskLevelTask::class.simpleName
        private val STALE_DOWNLOAD_LIMIT = Duration.standardHours(48)
    }
}
