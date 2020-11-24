package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.RiskLevelCalculationException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
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

class RiskLevelTask @Inject constructor(
    private val riskLevels: RiskLevels,
    @AppContext private val context: Context,
    private val enfClient: ENFClient,
    private val timeStamper: TimeStamper,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val riskLevelData: RiskLevelData,
    private val appConfigProvider: AppConfigProvider,
    private val exposureResultStore: ExposureResultStore
) : Task<DefaultProgress, RiskLevelTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result {
        try {
            Timber.d("Running with arguments=%s", arguments)
            // If there is no connectivity the transaction will set the last calculated risk level
            if (!isNetworkEnabled(context)) {
                RiskLevelRepository.setLastCalculatedRiskLevelAsCurrent()
                return Result(UNDETERMINED)
            }

            if (!enfClient.isTracingEnabled.first()) {
                return Result(NO_CALCULATION_POSSIBLE_TRACING_OFF)
            }

            val configData: ConfigData = appConfigProvider.getAppConfig()

            return Result(
                when {
                    calculationNotPossibleBecauseOfOutdatedResults().also {
                        checkCancel()
                    } -> if (backgroundJobsEnabled()) {
                        UNKNOWN_RISK_OUTDATED_RESULTS
                    } else {
                        UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
                    }

                    isIncreasedRisk(configData).also {
                        checkCancel()
                    } -> INCREASED_RISK

                    else -> LOW_LEVEL_RISK
                }.also {
                    checkCancel()
                    updateRepository(it, timeStamper.nowUTC.millis)
                    riskLevelData.lastUsedConfigIdentifier = configData.identifier
                }
            )
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
        // if the last calculation is longer in the past as the defined threshold we return the stale state
        val timeSinceLastDiagnosisKeyFetchFromServer =
            TimeVariables.getTimeSinceLastDiagnosisKeyFetchFromServer()
                ?: throw RiskLevelCalculationException(
                    IllegalArgumentException("Time since last exposure calculation is null")
                )
        /** we only return outdated risk level if the threshold is reached AND the active tracing time is above the
        defined threshold because [LOW_LEVEL_RISK] overrules [UNKNOWN_RISK_OUTDATED_RESULTS] */
        return timeSinceLastDiagnosisKeyFetchFromServer.millisecondsToHours() >
            TimeVariables.getMaxStaleExposureRiskRange() && isActiveTracingTimeAboveThreshold()
    }

    private fun isActiveTracingTimeAboveThreshold(): Boolean {
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

    private suspend fun isIncreasedRisk(configData: ExposureWindowRiskCalculationConfig): Boolean {
        val exposureWindows = enfClient.exposureWindows()

        return riskLevels.determineRisk(configData, exposureWindows).apply {
            // TODO This should be solved differently, by saving a more specialised result object
            if (isIncreasedRisk()) {
                exposureResultStore.internalMatchedKeyCount.value = totalMinimumDistinctEncountersWithHighRisk
                exposureResultStore.internalDaysSinceLastExposure.value = numberOfDaysWithHighRisk
            } else {
                exposureResultStore.internalMatchedKeyCount.value = totalMinimumDistinctEncountersWithLowRisk
                exposureResultStore.internalDaysSinceLastExposure.value = numberOfDaysWithLowRisk
            }
            exposureResultStore.entities.value = ExposureResult(exposureWindows, this)
        }.isIncreasedRisk()
    }

    private fun updateRepository(riskLevel: RiskLevel, time: Long) {
        val rollbackItems = mutableListOf<RollbackItem>()
        try {
            Timber.tag(TAG).v("Update the risk level with $riskLevel")
            val lastCalculatedRiskLevelScoreForRollback = RiskLevelRepository.getLastCalculatedScore()
            updateRiskLevelScore(riskLevel)
            rollbackItems.add {
                updateRiskLevelScore(lastCalculatedRiskLevelScoreForRollback)
            }

            // risk level calculation date update
            val lastCalculatedRiskLevelDate = LocalData.lastTimeRiskLevelCalculation()
            LocalData.lastTimeRiskLevelCalculation(time)
            rollbackItems.add {
                LocalData.lastTimeRiskLevelCalculation(lastCalculatedRiskLevelDate)
            }
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Updating the RiskLevelRepository failed.")

            try {
                Timber.tag(TAG).d("Initiate Rollback")
                for (rollbackItem: RollbackItem in rollbackItems) rollbackItem.invoke()
            } catch (rollbackException: Exception) {
                Timber.tag(TAG).e(rollbackException, "RiskLevelRepository rollback failed.")
            }

            throw error
        }
    }

    /**
     * Updates the Risk Level Score in the repository with the calculated Risk Level
     *
     * @param riskLevel
     */
    @VisibleForTesting
    internal fun updateRiskLevelScore(riskLevel: RiskLevel) {
        val lastCalculatedScore = RiskLevelRepository.getLastCalculatedScore()
        Timber.d("last CalculatedS core is ${lastCalculatedScore.raw} and Current Risk Level is ${riskLevel.raw}")

        if (RiskLevel.riskLevelChangedBetweenLowAndHigh(lastCalculatedScore, riskLevel) &&
            !LocalData.submissionWasSuccessful()
        ) {
            Timber.d(
                "Notification Permission = ${
                    NotificationManagerCompat.from(CoronaWarnApplication.getAppContext()).areNotificationsEnabled()
                }"
            )

            NotificationHelper.sendNotification(
                CoronaWarnApplication.getAppContext().getString(R.string.notification_body)
            )

            Timber.d("Risk level changed and notification sent. Current Risk level is ${riskLevel.raw}")
        }
        if (lastCalculatedScore.raw == RiskLevelConstants.INCREASED_RISK &&
            riskLevel.raw == RiskLevelConstants.LOW_LEVEL_RISK
        ) {
            LocalData.isUserToBeNotifiedOfLoweredRiskLevel = true

            Timber.d("Risk level changed LocalData is updated. Current Risk level is ${riskLevel.raw}")
        }
        RiskLevelRepository.setRiskLevelScore(riskLevel)
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

    class Result(val riskLevel: RiskLevel) : Task.Result {
        override fun toString(): String {
            return "Result(riskLevel=${riskLevel.name})"
        }
    }

    data class Config(
        // TODO unit-test that not > 9 min
        override val executionTimeout: Duration = Duration.standardMinutes(8),

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING

    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<RiskLevelTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG: String? = RiskLevelTask::class.simpleName
    }
}
