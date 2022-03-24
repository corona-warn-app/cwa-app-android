package de.rki.coronawarnapp.coronatest.type.pcr.execution

import androidx.annotation.VisibleForTesting
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.PersonalTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.type.common.ResultScheduler
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
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
class PCRResultScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val personalTestRepository: PersonalTestRepository,
) : ResultScheduler(
    workManager = workManager
) {

    @VisibleForTesting
    internal val shouldBePolling = personalTestRepository.latestPCRT
        .map { test: PCRCoronaTest? ->
            if (test == null) return@map false
            !test.isRedeemed
        }
        .distinctUntilChanged()

    fun setup() {
        Timber.tag(TAG).i("setup() - PCRResultScheduler")
        shouldBePolling
            .onEach { shouldBePolling ->
                Timber.tag(TAG).d("Polling state change: shouldBePolling=$shouldBePolling")
                setPcrPeriodicTestPollingEnabled(enabled = shouldBePolling)
            }
            .launchIn(appScope)
    }

    internal suspend fun setPcrPeriodicTestPollingEnabled(enabled: Boolean) {
        Timber.tag(TAG).i("setPcrPeriodicTestPollingEnabled(enabled=$enabled)")
        if (enabled) {
            val isScheduled = isScheduled(PCR_TESTRESULT_WORKER_UNIQUEUNAME)
            Timber.tag(TAG).d("isScheduled=$isScheduled")

            Timber.tag(TAG).d("enqueueUniquePeriodicWork PCR_TESTRESULT_WORKER_UNIQUEUNAME")
            workManager.enqueueUniquePeriodicWork(
                PCR_TESTRESULT_WORKER_UNIQUEUNAME,
                ExistingPeriodicWorkPolicy.KEEP,
                buildPcrTestResultRetrievalPeriodicWork()
            )
        } else {
            Timber.tag(TAG).d("cancelWorker()")
            workManager.cancelUniqueWork(PCR_TESTRESULT_WORKER_UNIQUEUNAME)
        }
    }

    private fun buildPcrTestResultRetrievalPeriodicWork() =
        PeriodicWorkRequestBuilder<PCRResultRetrievalWorker>(
            getPcrTestResultRetrievalPeriodicWorkTimeInterval(),
            TimeUnit.MINUTES
        )
            .addTag(PCR_TESTRESULT_WORKER_TAG)
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

    companion object {
        private const val PCR_TESTRESULT_WORKER_TAG = "DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER"
        private const val PCR_TESTRESULT_WORKER_UNIQUEUNAME = "DiagnosisTestResultBackgroundPeriodicWork"

        private const val TAG = "PCRTestResultScheduler"

        private const val MINUTES_IN_DAY = 1440
        private const val DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY = 12

        @VisibleForTesting
        internal fun getPcrTestResultRetrievalPeriodicWorkTimeInterval() =
            (MINUTES_IN_DAY / DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY).toLong()
    }
}
