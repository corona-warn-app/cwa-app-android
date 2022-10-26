package de.rki.coronawarnapp.coronatest.type.rapidantigen.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler.RatPollingMode.PHASE1
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler.RatPollingMode.PHASE2
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration

/**
 * RAT result retrieval by periodic polling
 */
class RAResultRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coronaTestRepository: CoronaTestRepository,
    private val timeStamper: TimeStamper,
    private val ratResultScheduler: RAResultScheduler,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("$id doWork() failed after $runAttemptCount attempts. Resuming at normal period.")
            return Result.failure()
        }

        try {
            val rat = coronaTestRepository.latestRAT.first()
            Timber.tag(TAG).v("Current RA test: %s", rat)

            if (rat == null) {
                // RAResultScheduler will cancel us if the test is null or isFinal==true
                Timber.tag(TAG).w("There is no RapidAntigen test available!?")
                return Result.success()
            }
            Timber.tag(TAG).v("$id Running RA test result refresh.")
            coronaTestRepository.refresh(BaseCoronaTest.Type.RAPID_ANTIGEN)
            Timber.tag(TAG).d("$id: RA test result refreshed.")

            val nowUTC = timeStamper.nowUTC
            val days = Duration.between(rat.registeredAt, nowUTC).toDays()
            val minutes = Duration.between(rat.registeredAt, nowUTC).toMinutes()
            val isPhase1 = ratResultScheduler.ratResultPeriodicPollingMode == PHASE1
            Timber.tag(TAG).d("Calculated days: %d", days)

            // Time for phase2?
            if (isPhase1 && minutes >= RAT_POLLING_END_OF_PHASE1_MINUTES) {
                Timber.tag(TAG).d("$id $minutes minutes - time for a phase 2!")
                ratResultScheduler.setRatResultPeriodicPollingMode(mode = PHASE2)
            }

            return Result.success()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Test result retrieval worker failed.")
            return Result.retry()
        }
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RAResultRetrievalWorker>

    companion object {
        private val TAG = RAResultRetrievalWorker::class.java.simpleName

        /**
         * The time when rat polling is switched to a larger interval
         */
        private const val RAT_POLLING_END_OF_PHASE1_MINUTES = 90
    }
}
