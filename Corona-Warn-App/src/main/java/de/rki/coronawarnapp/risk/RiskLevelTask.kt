package de.rki.coronawarnapp.risk

import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.ConnectivityHelper.isNetworkEnabled
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AppContext
class RiskLevelTask @Inject constructor(
    private val riskLevels: RiskLevels,
    private val context: Context
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
            with(riskLevels) {
                return Result(
                    when {
                        calculationNotPossibleBecauseTracingIsOff().also {
                            checkCancel()
                        } -> NO_CALCULATION_POSSIBLE_TRACING_OFF

                        calculationNotPossibleBecauseNoKeys().also {
                            checkCancel()
                        } -> UNKNOWN_RISK_INITIAL

                        calculationNotPossibleBecauseOfOutdatedResults().also {
                            checkCancel()
                        } -> if (backgroundJobsEnabled)
                            UNKNOWN_RISK_OUTDATED_RESULTS
                        else
                            UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL

                        isIncreasedRisk().also {
                            checkCancel()
                        } -> INCREASED_RISK

                        !isActiveTracingTimeAboveThreshold().also {
                            checkCancel()
                        } -> UNKNOWN_RISK_INITIAL

                        else -> LOW_LEVEL_RISK
                    }.also {
                        checkCancel()
                        updateRepository(it, System.currentTimeMillis())
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

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    private val backgroundJobsEnabled: Boolean
        get() = ConnectivityHelper.autoModeEnabled(CoronaWarnApplication.getAppContext()).also {
            if (it) {
                Timber.tag(TAG)
                    .v("diagnosis keys outdated and active tracing time is above threshold")
                Timber.tag(TAG)
                    .v("manual mode not active (background jobs enabled)")
            } else {
                Timber.tag(TAG)
                    .v("diagnosis keys outdated and active tracing time is above threshold")
                Timber.tag(TAG).v("manual mode active (background jobs disabled)")
            }
        }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    class Result(val riskLevel: RiskLevel) : Task.Result

    data class Config(
        // TODO unit-test that not > 9 min
        @Suppress("MagicNumber")
        override val executionTimeout: Duration = Duration.standardMinutes(8),

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING

    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<RiskLevelTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override val config: TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG: String? = RiskLevelTask::class.simpleName
    }
}
