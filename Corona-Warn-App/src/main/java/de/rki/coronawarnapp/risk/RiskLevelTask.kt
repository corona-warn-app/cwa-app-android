package de.rki.coronawarnapp.risk

import android.content.Context
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.RiskLevelCalculationException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
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
    private val appConfigProvider: AppConfigProvider
) : Task<DefaultProgress, RiskLevelTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result {
        try {
            Timber.d("Running with arguments=%s", arguments)
            // If there is no connectivity the transaction will set the last calculated
            // risk level
            if (!isNetworkEnabled(context)) {
                RiskLevelRepository.setLastCalculatedRiskLevelAsCurrent()
                return Result(UNDETERMINED)
            }

            if (!enfClient.isTracingEnabled.first()) {
                return Result(NO_CALCULATION_POSSIBLE_TRACING_OFF)
            }

            val configData: ConfigData = appConfigProvider.getAppConfig()

            with(riskLevels) {
                return Result(
                    when {
                        calculationNotPossibleBecauseOfNoKeys().also {
                            checkCancel()
                        } -> UNKNOWN_RISK_INITIAL

                        calculationNotPossibleBecauseOfOutdatedResults().also {
                            checkCancel()
                        } -> if (backgroundJobsEnabled()) {
                            UNKNOWN_RISK_OUTDATED_RESULTS
                        } else {
                            UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
                        }

                        isIncreasedRisk(getNewExposureSummary(), configData).also {
                            checkCancel()
                        } -> INCREASED_RISK

                        !isActiveTracingTimeAboveThreshold().also {
                            checkCancel()
                        } -> UNKNOWN_RISK_INITIAL

                        else -> LOW_LEVEL_RISK
                    }.also {
                        checkCancel()
                        updateRepository(it, timeStamper.nowUTC.millis)
                        riskLevelData.lastUsedConfigIdentifier = configData.identifier
                    }
                )
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

    /**
     * If there is no persisted exposure summary we try to get a new one with the last persisted
     * Google API token that was used in the [de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction]
     *
     * @return a exposure summary from the Google Exposure Notification API
     */
    private suspend fun getNewExposureSummary(): ExposureSummary {
        val googleToken = LocalData.googleApiToken()
            ?: throw RiskLevelCalculationException(IllegalStateException("Exposure summary is not persisted"))

        val exposureSummary =
            InternalExposureNotificationClient.asyncGetExposureSummary(googleToken)

        return exposureSummary.also {
            Timber.tag(TAG).v("Generated new exposure summary with $googleToken")
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
