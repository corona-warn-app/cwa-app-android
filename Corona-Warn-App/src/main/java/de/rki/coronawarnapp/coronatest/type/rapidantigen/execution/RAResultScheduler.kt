package de.rki.coronawarnapp.coronatest.type.rapidantigen.execution

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.type.common.ResultScheduler
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler.RatPollingMode.DISABLED
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler.RatPollingMode.PHASE1
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RAResultScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val coronaTestRepository: CoronaTestRepository,
) : ResultScheduler(
        workManager = workManager
    ),
    Initializer {

    private var ratWorkerMode = DISABLED
    val ratResultPeriodicPollingMode
        get() = ratWorkerMode

    enum class RatPollingMode {
        DISABLED,
        PHASE1,
        PHASE2
    }

    override fun initialize() {
        Timber.tag(TAG).d("setup() - RAResultScheduler")
        coronaTestRepository.latestRAT
            .map { test: RACoronaTest? ->
                if (test == null) return@map false
                !test.isRedeemed
            }
            .distinctUntilChanged()
            .onEach { shouldBePolling ->
                val isScheduled = isScheduled(RAT_RESULT_WORKER_UNIQUEUNAME)
                Timber.tag(TAG).d("Polling state change: shouldBePolling=$shouldBePolling, isScheduled=$isScheduled")

                if (shouldBePolling && isScheduled) {
                    Timber.tag(TAG).d("We are already scheduled, no changing MODE.")
                } else if (shouldBePolling && !isScheduled) {
                    Timber.tag(TAG).d("We should be polling, but are not scheduled, scheduling...")
                    setRatResultPeriodicPollingMode(PHASE1)
                } else {
                    Timber.tag(TAG).d("We should not be polling, canceling...")
                    setRatResultPeriodicPollingMode(DISABLED)
                }
            }
            .launchIn(appScope)
    }

    internal fun setRatResultPeriodicPollingMode(mode: RatPollingMode) {
        Timber.tag(TAG).i("setRatResultPeriodicPollingMode(mode=%s)", mode)
        ratWorkerMode = mode
        if (mode == DISABLED) {
            Timber.tag(TAG).d("cancelWorker()")
            workManager.cancelUniqueWork(RAT_RESULT_WORKER_UNIQUEUNAME)
        } else {
            // no check for already running workers!
            // worker must be replaced by next phase instance
            Timber.tag(TAG).d("Queueing rat result worker (RAT_RESULT_PERIODIC_WORKER)")
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
        return PeriodicWorkRequestBuilder<RAResultRetrievalWorker>(
            repeatInterval,
            TimeUnit.MINUTES
        )
            .addTag(RAT_RESULT_WORKER_TAG)
            .setConstraints(
                Constraints.Builder().apply {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }.build()
            )
            .setInitialDelay(
                TEST_RESULT_PERIODIC_INITIAL_DELAY,
                TimeUnit.SECONDS
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .build()
    }

    companion object {
        private const val RAT_RESULT_WORKER_TAG = "RAT_RESULT_PERIODIC_WORKER"
        private const val RAT_RESULT_WORKER_UNIQUEUNAME = "RatResultRetrievalWorker"

        private const val TAG = "RAResultScheduler"

        private const val ratResultRetrievalPeriodicWorkPhase1IntervalInMinutes = 15L

        private const val ratResultRetrievalPeriodicWorkPhase2IntervalInMinutes = 90L
    }
}
