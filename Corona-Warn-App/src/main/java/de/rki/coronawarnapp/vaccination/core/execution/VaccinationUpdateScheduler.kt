package de.rki.coronawarnapp.vaccination.core.execution

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
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

        // If there is a pending check, we can perform it in the background.
        // We basically consume all "pending check flags" in the background until there are none.
        vaccinationRepository.vaccinationInfos
            .map { vaccinatedPersons ->
                vaccinatedPersons.any { it.isProofCertificateCheckPending }
            }
            .distinctUntilChanged()
            .onEach { hasProofCheckPending ->
                val alreadyScheduled = isScheduled()
                Timber.tag(TAG).d("Enable worker? hasPending=$hasProofCheckPending, scheduled=$alreadyScheduled")
                setPeriodicUpdatesEnabled(hasProofCheckPending)
            }
            .catch { Timber.tag(TAG).e(it, "Failed to monitor for pending proof checks.") }
            .launchIn(appScope)

        // If there is a pending check or we have stale data, we refresh immediately when opening the app
        combine(
            // Pending checks?
            vaccinationRepository.vaccinationInfos.map { persons ->
                persons.any { it.isProofCertificateCheckPending }
            }.distinctUntilChanged(),
            // Stale data?
            vaccinationRepository.vaccinationInfos.map { persons ->
                val nowUTC = timeStamper.nowUTC
                persons.any {
                    it.lastProofCheckAt.toLocalDateUtc() != nowUTC.toLocalDateUtc() && it.isEligbleForProofCertificate
                }
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
