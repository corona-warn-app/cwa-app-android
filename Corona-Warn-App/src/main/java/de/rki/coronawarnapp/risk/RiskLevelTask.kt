package de.rki.coronawarnapp.risk

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.RiskLevelCalculationException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.BackgroundModeStatus
import de.rki.coronawarnapp.util.ConnectivityHelper.isNetworkEnabled
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHours
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
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
    private val riskLevelStorage: RiskLevelStorage
) : Task<DefaultProgress, RiskLevelTaskResult> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    @Suppress("LongMethod")
    override suspend fun run(arguments: Task.Arguments): RiskLevelTaskResult {
        try {
            Timber.d("Running with arguments=%s", arguments)
            if (!isNetworkEnabled(context)) {
                return RiskLevelTaskResult(
                    riskLevel = UNDETERMINED,
                    calculatedAt = timeStamper.nowUTC
                )
            }

            if (!enfClient.isTracingEnabled.first()) {
                return RiskLevelTaskResult(
                    riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
                    calculatedAt = timeStamper.nowUTC
                )
            }

            val configData: ConfigData = appConfigProvider.getAppConfig()

            return kotlin.run evaluation@{
                if (calculationNotPossibleBecauseOfNoKeys()) {
                    return@evaluation RiskLevelTaskResult(
                        riskLevel = UNKNOWN_RISK_INITIAL,
                        calculatedAt = timeStamper.nowUTC
                    )
                }

                if (calculationNotPossibleBecauseOfOutdatedResults()) {
                    return@evaluation RiskLevelTaskResult(
                        riskLevel = when (backgroundJobsEnabled()) {
                            true -> UNKNOWN_RISK_OUTDATED_RESULTS
                            false -> UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
                        },
                        calculatedAt = timeStamper.nowUTC
                    )
                }
                checkCancel()

                val risklevelResult = calculateRiskLevel(configData)
                if (risklevelResult.isIncreasedRisk) {
                    return@evaluation risklevelResult
                }
                checkCancel()

                if (!isActiveTracingTimeAboveThreshold()) {
                    return@evaluation RiskLevelTaskResult(
                        riskLevel = UNKNOWN_RISK_INITIAL,
                        calculatedAt = timeStamper.nowUTC
                    )
                }
                checkCancel()

                return@evaluation risklevelResult
            }.also {
                checkCancel()
                Timber.i("Evaluation finished with %s", it)

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
    }

    private fun calculationNotPossibleBecauseOfOutdatedResults(): Boolean {
        Timber.tag(TAG).d("Evaluating calculationNotPossibleBecauseOfOutdatedResults()")
        // if the last calculation is longer in the past as the defined threshold we return the stale state
        val timeSinceLastDiagnosisKeyFetchFromServer =
            TimeVariables.getTimeSinceLastDiagnosisKeyFetchFromServer()
                ?: throw RiskLevelCalculationException(
                    IllegalArgumentException("Time since last exposure calculation is null")
                )
        /** we only return outdated risk level if the threshold is reached AND the active tracing time is above the
        defined threshold because [UNKNOWN_RISK_INITIAL] overrules [UNKNOWN_RISK_OUTDATED_RESULTS] */
        return (timeSinceLastDiagnosisKeyFetchFromServer.millisecondsToHours() >
            TimeVariables.getMaxStaleExposureRiskRange() && isActiveTracingTimeAboveThreshold()).also {
            if (it) {
                Timber.tag(TAG).i("Calculation was not possible because reults are outdated.")
            } else {
                Timber.tag(TAG).d("Results are not out dated, continuing evaluation.")
            }
        }
    }

    private fun calculationNotPossibleBecauseOfNoKeys(): Boolean {
        Timber.tag(TAG).d("Evaluating calculationNotPossibleBecauseOfNoKeys()")
        return (TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() == null).also {
            if (it) {
                Timber.tag(TAG).v("No last time diagnosis keys from server fetch timestamp was found")
            } else {
                Timber.tag(TAG).d("Diagnosis keys from server are available, continuing evaluation.")
            }
        }
    }

    private fun isActiveTracingTimeAboveThreshold(): Boolean {
        Timber.tag(TAG).d("Evaluating isActiveTracingTimeAboveThreshold()")

        val durationTracingIsActive = TimeVariables.getTimeActiveTracingDuration()
        val activeTracingDurationInHours = durationTracingIsActive.millisecondsToHours()
        val durationTracingIsActiveThreshold = TimeVariables.getMinActivatedTracingTime().toLong()

        return (activeTracingDurationInHours >= durationTracingIsActiveThreshold).also {
            Timber.tag(TAG).v(
                "Active tracing time ($activeTracingDurationInHours h) is above threshold " +
                    "($durationTracingIsActiveThreshold h): $it"
            )
        }
    }

    private suspend fun calculateRiskLevel(configData: ExposureWindowRiskCalculationConfig): RiskLevelTaskResult {
        Timber.tag(TAG).d("Evaluating isIncreasedRisk(...)")
        val exposureWindows = enfClient.exposureWindows()

        return riskLevels.determineRisk(configData, exposureWindows).let {
            Timber.tag(TAG).d("Evaluated increased risk: %s", it)
            if (it.isIncreasedRisk()) {
                Timber.tag(TAG).i("Risk is increased!")
            } else {
                Timber.tag(TAG).d("Risk is not increased, continuing evaluating.")
            }

            RiskLevelTaskResult(
                riskLevel = if (it.isIncreasedRisk()) INCREASED_RISK else LOW_LEVEL_RISK,
                aggregatedRiskResult = it,
                exposureWindows = exposureWindows,
                calculatedAt = timeStamper.nowUTC
            )
        }
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
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

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Config(
        // TODO unit-test that not > 9 min
        override val executionTimeout: Duration = Duration.standardMinutes(8),

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING

    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<RiskLevelTask>
    ) : TaskFactory<DefaultProgress, RiskLevelTaskResult> {

        override suspend fun createConfig(): TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, RiskLevelTaskResult> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG: String? = RiskLevelTask::class.simpleName
    }
}
