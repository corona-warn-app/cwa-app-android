package de.rki.coronawarnapp.coronatest.worker.execution

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.worker.RatResultRetrievalWorker
import de.rki.coronawarnapp.coronatest.worker.execution.RatResultScheduler.RatPollingMode.DISABLED
import de.rki.coronawarnapp.coronatest.worker.execution.RatResultScheduler.RatPollingMode.PHASE1
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class RatResultScheduler @Inject constructor(
    private val workManager: WorkManager,
) {

    private var ratWorkerMode = DISABLED
    val ratResultPeriodicPollingMode
        get() = ratWorkerMode

    enum class RatPollingMode {
        DISABLED,
        PHASE1,
        PHASE2
    }

    fun setRatResultPeriodicPollingMode(mode: RatPollingMode) {
        ratWorkerMode = mode
        if (mode == DISABLED) {
            Timber.tag(TAG).d("cancelWorker()")
            workManager.cancelUniqueWork(RAT_RESULT_WORKER_UNIQUEUNAME)
        } else {
            // no check for already running workers!
            // worker must be replaced by next phase instance
            Timber.tag(TAG).i("Queueing rat result worker (RAT_RESULT_PERIODIC_WORKER)")
            workManager.enqueueUniquePeriodicWork(
                RAT_RESULT_WORKER_UNIQUEUNAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                buildRatResultRetrievalPeriodicWork(mode)
            )
        }
    }

    private fun buildRatResultRetrievalPeriodicWork(pollingMode: RatPollingMode): PeriodicWorkRequest {
        val repeatInterval = if (pollingMode == PHASE1) {
            ratResultRetrievalPeriodicWorkPhase1IntervalInMinutes
        } else {
            ratResultRetrievalPeriodicWorkPhase2IntervalInMinutes
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
        private const val RAT_RESULT_WORKER_TAG = "RAT_RESULT_PERIODIC_WORKER"
        private const val RAT_RESULT_WORKER_UNIQUEUNAME = "RatResultRetrievalWorker"

        private const val TAG = "RatResultScheduler"

        private const val ratResultRetrievalPeriodicWorkPhase1IntervalInMinutes = 15L

        private const val ratResultRetrievalPeriodicWorkPhase2IntervalInMinutes = 90L
    }
}
