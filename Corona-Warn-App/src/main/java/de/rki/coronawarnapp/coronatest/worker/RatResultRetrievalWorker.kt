package de.rki.coronawarnapp.coronatest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.worker.execution.RatResultScheduler
import de.rki.coronawarnapp.coronatest.worker.execution.RatResultScheduler.RatPollingMode.DISABLED
import de.rki.coronawarnapp.coronatest.worker.execution.RatResultScheduler.RatPollingMode.PHASE1
import de.rki.coronawarnapp.coronatest.worker.execution.RatResultScheduler.RatPollingMode.PHASE2
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import timber.log.Timber

/**
 * Diagnosis test result retrieval by periodic polling
 */
class RatResultRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coronaTestRepository: CoronaTestRepository,
    private val timeStamper: TimeStamper,
    private val ratResultScheduler: RatResultScheduler,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("$id doWork() failed after $runAttemptCount attempts. Rescheduling")

            ratResultScheduler.setRatResultPeriodicPollingMode(mode = ratResultScheduler.ratResultPeriodicPollingMode)
            Timber.tag(TAG).d("$id Rescheduled background worker")

            return Result.failure()
        }

        // checking abort conditions
        val rat = coronaTestRepository.latestRAT.first()
        if (rat == null) {
            Timber.tag(TAG).w("There is no PCR test available!?")
            disablePolling()
            return Result.success()
        } else {
            val nowUTC = timeStamper.nowUTC
            val days = Duration(rat.registeredAt, nowUTC).standardDays
            val minutes = Duration(rat.registeredAt, nowUTC).standardMinutes
            val isPhase1 = ratResultScheduler.ratResultPeriodicPollingMode == PHASE1
            Timber.tag(TAG).d("Calculated days: %d", days)
            when {
                rat.isResultAvailableNotificationSent -> {
                    Timber.tag(TAG).d("$id: Notification already sent.")
                    disablePolling()
                }
                rat.isViewed -> {
                    Timber.tag(TAG).d("$id: Test result has already been viewed.")
                    disablePolling()
                }
                days >= BackgroundConstants.POLLING_VALIDITY_MAX_DAYS -> {
                    Timber.tag(TAG).d("$id $days is exceeding the maximum polling duration")
                    disablePolling()
                }
                isPhase1 && minutes >= RAT_POLLING_END_OF_PHASE1_MINUTES -> {
                    Timber.tag(TAG).d("$id $minutes minutes - time for a phase 2!")
                    ratResultScheduler.setRatResultPeriodicPollingMode(mode = PHASE2)
                }
                else -> {
                    coronaTestRepository.refresh(CoronaTest.Type.RAPID_ANTIGEN)
                }
            }
            return Result.success()
        }
    }

    private fun disablePolling() {
        ratResultScheduler.setRatResultPeriodicPollingMode(mode = DISABLED)
        Timber.tag(TAG).d("$id: Background worker stopped")
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RatResultRetrievalWorker>

    companion object {
        private val TAG = RatResultRetrievalWorker::class.java.simpleName

        /**
         * The time when rat polling is switched to a larger interval
         *
         * @see TimeUnit.MINUTES
         */
        private const val RAT_POLLING_END_OF_PHASE1_MINUTES = 90
    }
}
