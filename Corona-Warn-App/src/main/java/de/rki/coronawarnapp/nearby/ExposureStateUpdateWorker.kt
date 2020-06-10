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
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import timber.log.Timber

class ExposureStateUpdateWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    companion object {
        private val TAG = ExposureStateUpdateWorker::class.simpleName
    }

    override suspend fun doWork(): Result {
        try {
            Timber.v("worker to persist exposure summary started")
            val token = inputData.getString(ExposureNotificationClient.EXTRA_TOKEN)
                ?: throw NoTokenException(IllegalArgumentException("no token was found in the intent"))

            Timber.v("valid token $token retrieved")

            val exposureSummary = InternalExposureNotificationClient
                .asyncGetExposureSummary(token)

            ExposureSummaryRepository.getExposureSummaryRepository()
                .insertExposureSummaryEntity(exposureSummary)
            Timber.v("exposure summary state updated: $exposureSummary")

            RiskLevelTransaction.start()
            Timber.v("risk level calculation triggered")
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
        }

        return Result.success()
    }
}
