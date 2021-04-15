package de.rki.coronawarnapp.coronatest.execution

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import de.rki.coronawarnapp.worker.DiagnosisTestResultRetrievalPeriodicWorker
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class TestResultScheduler @Inject constructor(
    private val tracingSettings: TracingSettings,
    private val workManager: WorkManager,
    private val timeStamper: TimeStamper,
) {

    private suspend fun isScheduled(): Boolean {
        val workerInfos = workManager.getWorkInfosForUniqueWork(PCR_TESTRESULT_WORKER_UNIQUEUNAME).await()

        return workerInfos.any { it.isScheduled }
    }

    fun setPeriodicTestPolling(enabled: Boolean) {
        if (enabled) {
            // TODO Refactor runBlocking away
            val isScheduled = runBlocking { isScheduled() }
            if (isScheduled) {
                Timber.tag(TAG).w("Already scheduled, skipping")
                return
            }
            Timber.tag(TAG).i("Queueing test result worker (DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER)")
            workManager.enqueueUniquePeriodicWork(
                PCR_TESTRESULT_WORKER_UNIQUEUNAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                buildDiagnosisTestResultRetrievalPeriodicWork()
            )
        } else {
            Timber.tag(TAG).d("cancelWorker()")
            workManager.cancelUniqueWork(PCR_TESTRESULT_WORKER_UNIQUEUNAME)
        }
    }

    private fun buildDiagnosisTestResultRetrievalPeriodicWork() =
        PeriodicWorkRequestBuilder<DiagnosisTestResultRetrievalPeriodicWorker>(
            BackgroundWorkHelper.getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(),
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

    private val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED

    companion object {
        /**
         * Kind initial delay in minutes for periodic work for accessibility reason
         *
         * @see TimeUnit.SECONDS
         */
        private const val DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY = 10L
        private const val PCR_TESTRESULT_WORKER_TAG = "DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER"
        private const val PCR_TESTRESULT_WORKER_UNIQUEUNAME = "DiagnosisTestResultBackgroundPeriodicWork"

        private const val TAG = "TestResultScheduler"
    }
}
