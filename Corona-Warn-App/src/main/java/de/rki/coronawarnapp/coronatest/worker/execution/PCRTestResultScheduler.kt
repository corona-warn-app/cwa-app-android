package de.rki.coronawarnapp.coronatest.worker.execution

import androidx.annotation.VisibleForTesting
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.worker.PCRTestResultRetrievalWorker
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class PCRTestResultScheduler @Inject constructor(
    private val workManager: WorkManager,
) {

    private suspend fun isPcrScheduled() =
        workManager.getWorkInfosForUniqueWork(PCR_TESTRESULT_WORKER_UNIQUEUNAME)
            .await()
            .any { it.isScheduled }

    private val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED

    fun setPcrPeriodicTestPollingEnabled(enabled: Boolean) {
        if (enabled) {
            // TODO Refactor runBlocking away
            val isScheduled = runBlocking { isPcrScheduled() }
            if (isScheduled) {
                Timber.tag(TAG).w("Already scheduled, skipping")
                return
            }
            Timber.tag(TAG).i("Queueing pcr test result worker (DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER)")
            workManager.enqueueUniquePeriodicWork(
                PCR_TESTRESULT_WORKER_UNIQUEUNAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                buildPcrTestResultRetrievalPeriodicWork()
            )
        } else {
            Timber.tag(TAG).d("cancelWorker()")
            workManager.cancelUniqueWork(PCR_TESTRESULT_WORKER_UNIQUEUNAME)
        }
    }

    private fun buildPcrTestResultRetrievalPeriodicWork() =
        PeriodicWorkRequestBuilder<PCRTestResultRetrievalWorker>(
            getPcrTestResultRetrievalPeriodicWorkTimeInterval(),
            TimeUnit.MINUTES
        )
            .addTag(PCR_TESTRESULT_WORKER_TAG)
            .setConstraints(BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork())
            .setInitialDelay(
                DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY,
                TimeUnit.SECONDS
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    companion object {
        /**
         * Kind initial delay in minutes for periodic work for accessibility reason
         *
         * @see TimeUnit.SECONDS
         */
        private const val DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY = 10L
        private const val PCR_TESTRESULT_WORKER_TAG = "DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER"
        private const val PCR_TESTRESULT_WORKER_UNIQUEUNAME = "DiagnosisTestResultBackgroundPeriodicWork"

        private const val TAG = "PCRTestResultScheduler"

        /**
         * Calculate the time for pcr diagnosis key retrieval periodic work
         *
         * @return Long
         *
         * @see BackgroundConstants.MINUTES_IN_DAY
         */
        @VisibleForTesting
        internal fun getPcrTestResultRetrievalPeriodicWorkTimeInterval(): Long =
            (
                BackgroundConstants.MINUTES_IN_DAY /
                    BackgroundConstants.DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY
                ).toLong()
    }
}
