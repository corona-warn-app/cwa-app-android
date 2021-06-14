package de.rki.coronawarnapp.covidcertificate.vaccination.core.execution.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.vaccination.core.execution.task.VaccinationUpdateTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class VaccinationUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        Timber.tag(TAG).v("$id: doWork() started. Run attempt: $runAttemptCount")

        Timber.tag(TAG).i("Running vaccination data update task.")
        val taskState = taskController.submitBlocking(
            DefaultTaskRequest(
                VaccinationUpdateTask::class,
                arguments = VaccinationUpdateTask.Arguments,
                errorHandling = TaskFactory.Config.ErrorHandling.SILENT,
                originTag = TAG,
            )
        )

        when {
            taskState.isSuccessful -> {
                Timber.tag(TAG).d("$id: VaccinationUpdateTask finished successfully.")
                Result.success()
            }
            else -> {
                taskState.error?.let {
                    Timber.tag(TAG).w(it, "$id: Error during VaccinationUpdateTask.")
                }
                Result.retry()
            }
        }
    } catch (e: Exception) {
        e.reportProblem(TAG, "VaccinationUpdateTask failed exceptionally, will retry.")
        Result.retry()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<VaccinationUpdateWorker>

    companion object {
        private val TAG = VaccinationUpdateWorker::class.java.simpleName
    }
}
