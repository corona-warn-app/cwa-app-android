package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.ExposureResult
import de.rki.coronawarnapp.risk.ExposureResultStore
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class ExposureStateUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val exposureResultStore: ExposureResultStore,
    private val enfClient: ENFClient,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Timber.tag(TAG).v("Worker to persist exposure summary started")
            enfClient.exposureWindows().let {
                exposureResultStore.entities.value = ExposureResult(it, null)
                Timber.tag(TAG).v("Exposure summary state updated: $it")
            }

            taskController.submit(
                DefaultTaskRequest(RiskLevelTask::class, originTag = "ExposureStateUpdateWorker")
            )
            Timber.tag(TAG).v("Risk level calculation triggered")
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        }

        return Result.success()
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<ExposureStateUpdateWorker>

    companion object {
        private val TAG = ExposureStateUpdateWorker::class.java.simpleName
    }
}
