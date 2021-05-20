package de.rki.coronawarnapp.vaccination.core.execution

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.vaccination.core.execution.task.VaccinationUpdateTask
import de.rki.coronawarnapp.vaccination.core.execution.worker.VaccinationUpdateWorkerRequestBuilder
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationUpdateScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val taskController: TaskController,
    private val vaccinationRepository: VaccinationRepository,
    private val foregroundState: ForegroundState,
    private val workManager: WorkManager,
    private val workerRequestBuilder: VaccinationUpdateWorkerRequestBuilder,
    private val timeStamper: TimeStamper,
) {

    fun setup() {
        Timber.tag(TAG).d("setup()")
        if (!CWADebug.isDeviceForTestersBuild) {
            // In tester builds we want to keep the logic intact, but in prod we don't need to activate it
            return
        }

        vaccinationRepository.vaccinationInfos
            .map {
                false // NOOP, but we want to keep the logic for now
            }
            .distinctUntilChanged()
            .onEach { hasPendingChecks ->
                val alreadyScheduled = isScheduled()
                Timber.tag(TAG).d("Enable worker? hasPending=$hasPendingChecks, scheduled=$alreadyScheduled")
                setPeriodicUpdatesEnabled(hasPendingChecks)
            }
            .catch { Timber.tag(TAG).e(it, "Failed to monitor for pending proof checks.") }
            .launchIn(appScope)

        // If there is a pending check or we have stale data, we refresh immediately when opening the app
        combine(
            // Pending checks?
            vaccinationRepository.vaccinationInfos.map {
                false // NOOP, but we want to keep the logic for now
            }.distinctUntilChanged(),
            // Stale data?
            vaccinationRepository.vaccinationInfos.map {
                false // NOOP, but we want to keep the logic for now
            }.distinctUntilChanged(),
            foregroundState.isInForeground
        ) { hasPending, staleData, isForeground ->
            Timber.tag(TAG).v("Run now? pending=$hasPending, staleData=$staleData, isForeground=$isForeground")

            if (isForeground && (hasPending || staleData)) {
                Timber.tag(TAG).d("App moved to foreground, with pending checks, initiating refresh.")
                DefaultTaskRequest(
                    type = VaccinationUpdateTask::class,
                    arguments = VaccinationUpdateTask.Arguments,
                    errorHandling = TaskFactory.Config.ErrorHandling.SILENT,
                    originTag = TAG,
                ).run { taskController.submit(this) }
            }
        }
            .catch { Timber.tag(TAG).e(it, "Failed to monitor foreground state changes.") }
            .launchIn(appScope)
    }

    private fun setPeriodicUpdatesEnabled(enabled: Boolean) {
        Timber.tag(TAG).i("setPeriodicUpdatesEnabled(enabled=$enabled)")
        if (enabled) {
            val request = workerRequestBuilder.createPeriodicWorkRequest()
            Timber.tag(TAG).d("queueWorker(request=%s)", request)
            workManager.enqueueUniquePeriodicWork(
                WORKER_ID_VACCINATION_UPDATE,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        } else {
            Timber.tag(TAG).d("cancelWorker()")
            workManager.cancelUniqueWork(WORKER_ID_VACCINATION_UPDATE)
        }
    }

    private suspend fun isScheduled(): Boolean = workManager
        .getWorkInfosForUniqueWork(WORKER_ID_VACCINATION_UPDATE)
        .await()
        .any { it.isScheduled }

    internal val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED

    companion object {
        private val TAG: String = VaccinationUpdateScheduler::class.java.simpleName
        private const val WORKER_ID_VACCINATION_UPDATE = "VaccinationUpdateWorker"
    }
}
