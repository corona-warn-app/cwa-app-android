package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.NoTokenException
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.di.AppInjector
import timber.log.Timber

class ExposureStateUpdateWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Timber.v("worker to persist exposure summary started")
            val token = inputData.getString(ExposureNotificationClient.EXTRA_TOKEN)
                ?: throw NoTokenException(IllegalArgumentException("no token was found in the intent"))
            Timber.v("valid token $token retrieved")
            InternalExposureNotificationClient
                .asyncGetExposureSummary(token).also {
                    ExposureSummaryRepository.getExposureSummaryRepository()
                        .insertExposureSummaryEntity(it)
                    Timber.v("exposure summary state updated: $it")
                }

            AppInjector.component.taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            Timber.v("risk level calculation triggered")
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
        }

        return Result.success()
    }
}
