package de.rki.coronawarnapp.coronatest.execution

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler.RatPollingMode.DISABLED
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler.RatPollingMode.PHASE1
import de.rki.coronawarnapp.coronatest.worker.PCRTestResultRetrievalWorker
import de.rki.coronawarnapp.coronatest.worker.RatResultRetrievalWorker
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class TestResultScheduler @Inject constructor(
    private val workManager: WorkManager,
) {

    private var pcrWorkerEnabled = false
    private var ratWorkerMode = DISABLED

    private suspend fun isPcrScheduled() =
        workManager.getWorkInfosForUniqueWork(PCR_TESTRESULT_WORKER_UNIQUEUNAME)
            .await()
            .any { it.isScheduled }

    private suspend fun isRatScheduled() =
        workManager.getWorkInfosForUniqueWork(RAT_RESULT_WORKER_UNIQUEUNAME)
            .await()
            .any { it.isScheduled }

    private val WorkInfo.isScheduled: Boolean
        get() = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED

    var pcrPeriodicTestPollingEnabled: Boolean
        get() = pcrWorkerEnabled
        set(value) {
            pcrWorkerEnabled = value
            if (value) {
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

    enum class RatPollingMode {
        DISABLED,
        PHASE1,
        PHASE2
    }

    var ratResultPeriodicPollingMode: RatPollingMode
        get() = ratWorkerMode
        set(value) {
            ratWorkerMode = value
            if (value == DISABLED) {
                Timber.tag(TAG).d("cancelWorker()")
                workManager.cancelUniqueWork(RAT_RESULT_WORKER_UNIQUEUNAME)
            } else {
                // TODO Refactor runBlocking away
                val isScheduled = runBlocking { isRatScheduled() }
                if (isScheduled) {
                    Timber.tag(TAG).w("Already scheduled, skipping")
                    return
                }
                Timber.tag(TAG).i("Queueing rat result worker (DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER)")
                workManager.enqueueUniquePeriodicWork(
                    RAT_RESULT_WORKER_UNIQUEUNAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    buildRatResultRetrievalPeriodicWork(value)
                )
            }
        }

    private fun buildPcrTestResultRetrievalPeriodicWork() =
        PeriodicWorkRequestBuilder<PCRTestResultRetrievalWorker>(
            BackgroundWorkHelper.getPcrTestResultRetrievalPeriodicWorkTimeInterval(),
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

    private fun buildRatResultRetrievalPeriodicWork(pollingMode: RatPollingMode): PeriodicWorkRequest {
        val repeatInterval = if (pollingMode == PHASE1) {
            BackgroundWorkHelper.ratResultRetrievalPeriodicWorkPhase1IntervalInMinutes
        } else {
            BackgroundWorkHelper.ratResultRetrievalPeriodicWorkPhase2IntervalInMinutes
        }
        return PeriodicWorkRequestBuilder<RatResultRetrievalWorker>(
            repeatInterval,
            TimeUnit.MINUTES
        )
            .addTag(RAT_RESULT_WORKER_TAG)
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
    }

    companion object {
        /**
         * Kind initial delay in minutes for periodic work for accessibility reason
         *
         * @see TimeUnit.SECONDS
         */
        private const val DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY = 10L
        private const val PCR_TESTRESULT_WORKER_TAG = "DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER"
        private const val RAT_RESULT_WORKER_TAG = "RAT_RESULT_PERIODIC_WORKER"
        private const val PCR_TESTRESULT_WORKER_UNIQUEUNAME = "DiagnosisTestResultBackgroundPeriodicWork"
        private const val RAT_RESULT_WORKER_UNIQUEUNAME = "RatResultRetrievalWorker"

        private const val TAG = "TestResultScheduler"
    }
}
