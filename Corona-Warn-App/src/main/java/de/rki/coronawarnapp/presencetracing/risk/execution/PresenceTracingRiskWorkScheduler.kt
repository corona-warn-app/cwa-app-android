package de.rki.coronawarnapp.presencetracing.risk.execution

import android.annotation.SuppressLint
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.risk.execution.RiskWorkScheduler
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PresenceTracingRiskWorkScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val taskController: TaskController,
    private val presenceWorkBuilder: PresenceTracingWarningWorkBuilder,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val presenceTracingSettings: TraceLocationSettings,
) : RiskWorkScheduler(
    workManager = workManager,
    logTag = TAG,
) {

    @SuppressLint("BinaryOperationInTimber")
    fun setup() {
        Timber.tag(TAG).i("setup() PresenceTracingRiskWorkScheduler")
        combine(
            backgroundModeStatus.isAutoModeEnabled,
            presenceTracingSettings.isOnboardingDoneFlow
        ) { isAutoMode, isPresenceTracingOnboarded ->
            Timber.tag(TAG).d(
                "isAutoMode=$isAutoMode, " +
                    "isPresenceTracingOnboarded=$isPresenceTracingOnboarded"
            )
            isAutoMode && isPresenceTracingOnboarded
        }.onEach { runPeriodicWorker ->
            Timber.tag(TAG).v("runPeriodicWorker=$runPeriodicWorker")
            setPeriodicRiskCalculation(enabled = runPeriodicWorker)
        }.launchIn(appScope)
    }

    fun runRiskTaskNow(sourceTag: String) = taskController.submit(
        DefaultTaskRequest(
            PresenceTracingWarningTask::class,
            originTag = "PresenceTracingRiskWorkScheduler-$sourceTag"
        )
    )

    override suspend fun isScheduled(): Boolean = workManager
        .getWorkInfosForUniqueWork(WORKER_ID_PRESENCE_TRACING)
        .await()
        .any { it.isScheduled }

    override fun setPeriodicRiskCalculation(enabled: Boolean) {
        Timber.tag(TAG).i("setPeriodicRiskCalculation(enabled=$enabled)")

        if (enabled) {
            val warningRequest = presenceWorkBuilder.createPeriodicWorkRequest()
            queueWorker(WORKER_ID_PRESENCE_TRACING, warningRequest)
        } else {
            cancelWorker(WORKER_ID_PRESENCE_TRACING)
        }
    }

    companion object {
        private const val WORKER_ID_PRESENCE_TRACING = "PresenceTracingWarningWorker"
        private const val TAG = "PTRiskWorkScheduler"
    }
}
