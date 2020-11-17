package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

class ExposureStateUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val enfClient: ENFClient,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Timber.v("worker to persist exposure summary started")
            enfClient.exposureWindows().also {
                    ExposureSummaryRepository.getExposureSummaryRepository()
                        .insertExposureSummaryEntity(it)
                    Timber.v("exposure summary state updated: $it")
                }

            taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            Timber.v("risk level calculation triggered")
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        }

        return Result.success()
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<ExposureStateUpdateWorker>
}
