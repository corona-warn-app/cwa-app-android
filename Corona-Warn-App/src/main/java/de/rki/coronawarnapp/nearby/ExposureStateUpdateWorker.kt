package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.EwRiskLevelTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class ExposureStateUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            taskController.submitBlocking(
                DefaultTaskRequest(EwRiskLevelTask::class, originTag = "ExposureStateUpdateWorker")
            )
            Timber.tag(TAG).v("Risk level calculation triggered")
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        }

        return Result.success()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<ExposureStateUpdateWorker>

    companion object {
        private val TAG = ExposureStateUpdateWorker::class.java.simpleName
    }
}
