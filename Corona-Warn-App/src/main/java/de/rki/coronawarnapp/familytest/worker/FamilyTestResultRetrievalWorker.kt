package de.rki.coronawarnapp.familytest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * Family test result retrieval every 2h for all test types
 */
class FamilyTestResultRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FamilyTestRepository,
    private val familyTestResultRetrievalScheduler: FamilyTestResultRetrievalScheduler,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.d("$id doWork() failed after $runAttemptCount attempts. Resuming at normal period.")
            return Result.failure()
        }

        return try {
            repository.refresh()
            Timber.d("$id: Family test results retrieval successful.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Family test results retrieval failed.")
            Result.retry()
        }.also {
            familyTestResultRetrievalScheduler.checkPollingSchedule()
        }
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<FamilyTestResultRetrievalWorker>
}
