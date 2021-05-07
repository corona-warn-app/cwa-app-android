package de.rki.coronawarnapp.vaccination.core.execution

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.vaccination.core.execution.task.VaccinationUpdateTask
import de.rki.coronawarnapp.vaccination.core.execution.worker.VaccinationUpdateWorkerRequestBuilder
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
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
) {

    fun setup() {
        Timber.tag(TAG).d("setup()")
        vaccinationRepository.vaccinationInfos
            .onEach { vaccinatedPersons ->
                setPeriodicUpdatesEnabled(vaccinatedPersons.isNotEmpty())
            }
            .catch { Timber.tag(TAG).e("Failed to monitor vaccination infos.") }
            .launchIn(appScope)

        foregroundState.isInForeground
            .onEach {
                taskController.submit(DefaultTaskRequest(VaccinationUpdateTask::class, originTag = TAG))
            }
            .catch { Timber.tag(TAG).e("Failed to monitor foreground state changes.") }
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
