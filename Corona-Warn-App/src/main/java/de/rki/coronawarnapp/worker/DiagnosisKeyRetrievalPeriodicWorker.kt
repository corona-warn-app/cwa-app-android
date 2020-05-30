package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report

/**
 * Periodic diagnosis key retrieval work
 * Executes the scheduling of one time diagnosis key retrieval work
 *
 * @see BackgroundWorkScheduler
 * @see DiagnosisKeyRetrievalOneTimeWorker
 */
class DiagnosisKeyRetrievalPeriodicWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = DiagnosisKeyRetrievalPeriodicWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     * @see BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
     */
    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "Background job started...")
        try {
            BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
        } catch (e: Exception) {
            e.report(ExceptionCategory.JOB)
            return Result.failure()
        }
        return Result.success()
    }
}
