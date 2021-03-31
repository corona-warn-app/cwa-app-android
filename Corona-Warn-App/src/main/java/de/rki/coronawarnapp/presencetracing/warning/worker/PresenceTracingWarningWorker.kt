package de.rki.coronawarnapp.presencetracing.warning.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

class PresenceTracingWarningWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        Timber.tag(TAG).v("TraceTimeWarningTask for download and calculation triggered")
        val taskState = taskController.submitBlocking(
            DefaultTaskRequest(PresenceTracingWarningTask::class, originTag = "TraceTimeWarningWorker")
        )

        when {
            taskState.isSuccessful -> {
                Result.success()
            }
            runAttemptCount < BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD -> {
                Result.retry()
            }
            else -> {
                Result.failure()
            }
        }
    } catch (e: ApiException) {
        e.reportProblem(TAG, "Failed to submit TraceTimeWarningTask")
        Result.failure()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<PresenceTracingWarningWorker>

    companion object {
        private val TAG = PresenceTracingWarningWorker::class.java.simpleName
    }
}
