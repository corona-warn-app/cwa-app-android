package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction

/**
 * One time diagnosis key retrieval work
 * Executes the retrieve diagnosis key transaction
 *
 * @see BackgroundWorkScheduler
 */
class DiagnosisKeyRetrievalOneTimeWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = DiagnosisKeyRetrievalOneTimeWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     * @see RetrieveDiagnosisKeysTransaction
     */
    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "Background job started...")
        try {
            RetrieveDiagnosisKeysTransaction.start()
        } catch (e: Exception) {
            e.report(ExceptionCategory.JOB)
            return Result.failure()
        }
        return Result.success()
    }
}
