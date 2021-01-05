package de.rki.coronawarnapp.submission.auto

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.task.SubmissionTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class SubmissionWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        Timber.tag(TAG).d("Attempting background submission of TEKs.")

        val result = taskController.submitBlocking(
            DefaultTaskRequest(
                type = SubmissionTask::class,
                arguments = SubmissionTask.Arguments(checkUserActivity = true),
                errorHandling = TaskFactory.Config.ErrorHandling.SILENT,
                originTag = TAG
            )
        )
        result.error?.let { throw it }

        Timber.tag(TAG).d("Submission task completed with: %s", result.result)
        Result.success()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "TEK submission failed.")
        e.report(ExceptionCategory.HTTP, prefix = "TEK Submission failed.")
        Result.retry()
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<SubmissionWorker>

    companion object {
        private const val TAG = "SubmissionWorker"
    }
}
