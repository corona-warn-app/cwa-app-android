package de.rki.coronawarnapp.risk.execution

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.diagnosiskeys.execution.DiagnosisKeyRetrievalWorkBuilder
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningWorkBuilder
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskWorkScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val taskController: TaskController,
    private val presenceWorkBuilder: PresenceTracingWarningWorkBuilder,
    private val diagnosisWorkBuilder: DiagnosisKeyRetrievalWorkBuilder,
) {

    suspend fun runRiskTasksNow(): List<TaskState> {
        val diagnosisKeysState = appScope.async {
            Timber.tag(TAG).d("Running DownloadDiagnosisKeysTask")
            val result = taskController.submitBlocking(
                DefaultTaskRequest(
                    DownloadDiagnosisKeysTask::class,
                    DownloadDiagnosisKeysTask.Arguments(),
                    originTag = "RiskWorkScheduler-runRiskTasksNow"
                )
            )
            Timber.tag(TAG).d("DownloadDiagnosisKeysTask finished with %s", result)
            result
        }
        val presenceWarningState = appScope.async {
            Timber.tag(TAG).d("Running PresenceTracingWarningTask")
            val result = taskController.submitBlocking(
                DefaultTaskRequest(
                    PresenceTracingWarningTask::class,
                    originTag = "RiskWorkScheduler-runRiskTasksNow"
                )
            )
            Timber.tag(TAG).d("PresenceTracingWarningTask finished with %s", result)
            result
        }
        return listOf(diagnosisKeysState, presenceWarningState).awaitAll()
    }

    suspend fun isScheduled(): Boolean {
        val diagnosisWorkerInfos = appScope.async {
            workManager.getWorkInfosForUniqueWork(WORKER_ID_PRESENCE_TRACING).await()
        }
        val warningWorkerInfos = appScope.async {
            workManager.getWorkInfosForUniqueWork(WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD).await()
        }
        return listOf(diagnosisWorkerInfos, warningWorkerInfos).awaitAll().all { perWorkerInfos ->
            perWorkerInfos.any { it.isScheduled }
        }
    }

    fun setPeriodicRiskCalculation(enabled: Boolean) {
        Timber.tag(TAG).i("setPeriodicRiskCalculation(enabled=$enabled)")

        if (enabled) {
            val diagnosisRequest = diagnosisWorkBuilder.createPeriodicWorkRequest()
            queueWorker(WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD, diagnosisRequest)

            val warningRequest = presenceWorkBuilder.createPeriodicWorkRequest()
            queueWorker(WORKER_ID_PRESENCE_TRACING, warningRequest)
        } else {
            cancelWorker(WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD)
            cancelWorker(WORKER_ID_PRESENCE_TRACING)
        }
    }

    private fun queueWorker(workerId: String, request: PeriodicWorkRequest) {
        Timber.tag(TAG).d("queueWorker(workerId=%s, request=%s)", workerId, request)
        workManager.enqueueUniquePeriodicWork(
            workerId,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun cancelWorker(workerId: String) {
        Timber.tag(TAG).d("cancelWorker(workerId=$workerId")
        workManager.cancelUniqueWork(workerId)
    }

    private val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED

    companion object {
        private const val WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD = "DiagnosisKeyBackgroundPeriodicWork"
        private const val WORKER_ID_PRESENCE_TRACING = "de.rki.coronawarnapp.worker.PresenceTracingWarningWorker"
        private const val TAG = "RiskWorkScheduler"
    }
}
