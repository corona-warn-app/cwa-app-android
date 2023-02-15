package de.rki.coronawarnapp.presencetracing.risk.execution

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import timber.log.Timber

@HiltWorker
class PresenceTracingWarningWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        Timber.tag(TAG).v("$id: doWork() started. Run attempt: $runAttemptCount")

        val taskState = taskController.submitBlocking(
            DefaultTaskRequest(PresenceTracingWarningTask::class, originTag = TAG)
        )

        when {
            taskState.isSuccessful -> {
                Timber.tag(TAG).d("$id: PresenceTracingWarningTask finished successfully.")
                Result.success()
            }

            else -> {
                taskState.error?.let {
                    Timber.tag(TAG).w(it, "$id: Error during PresenceTracingWarningTask.")
                }
                Result.retry()
            }
        }
    } catch (e: Exception) {
        e.reportProblem(TAG, "PresenceTracingWarningTask failed exceptionally, will retry.")
        Result.retry()
    }

    companion object {
        private val TAG = tag<PresenceTracingWarningWorker>()
    }
}
