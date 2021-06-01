package de.rki.coronawarnapp.coronatest.type.common

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.TestCertificateRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateRetrievalScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    certificateRepo: TestCertificateRepository,
    foregroundState: ForegroundState,
) : ResultScheduler(
    workManager = workManager
) {
    private val processedNewCerts = mutableSetOf<String>()

    private val shouldPollDcc = combine(
        certificateRepo.certificates,
        foregroundState.isInForeground,
    ) { certificates, isForeground ->

        val hasNewCert = certificates.any {
            val isNew = processedNewCerts.contains(it.identifier)
            if (isNew) processedNewCerts.add(it.identifier)
            isNew
        }

        val hasWorkToDo = certificates.any { it.isPending }
        Timber.tag(TAG).v("shouldPollDcc? hasNewCert=$hasNewCert, hasWorkTodo=$hasWorkToDo, foreground=$isForeground")
        (isForeground || hasNewCert) && hasWorkToDo
    }
        .distinctUntilChanged()

    fun setup() {
        Timber.tag(TAG).i("setup() - TestCertificateRetrievalScheduler")
        shouldPollDcc
            .onEach { checkCerts ->
                Timber.tag(TAG).d("State change: checkCerts=$checkCerts")
                if (checkCerts) scheduleWorker()
            }
            .launchIn(appScope)
    }

    internal suspend fun scheduleWorker() {
        Timber.tag(TAG).i("scheduleWorker()")

        if (isScheduled(WORKER_ID)) {
            Timber.tag(TAG).d("Worker already queued, skipping requeue.")
        }

        Timber.tag(TAG).d("enqueueUniqueWork PCR_TESTRESULT_WORKER_UNIQUEUNAME")
        workManager.enqueueUniqueWork(
            WORKER_ID,
            ExistingWorkPolicy.KEEP,
            buildWorkerRequest()
        )
    }

    private fun buildWorkerRequest() =
        OneTimeWorkRequestBuilder<TestCertificateRetrievalWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setInitialDelay(TEST_RESULT_PERIODIC_INITIAL_DELAY, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, BackgroundConstants.KIND_DELAY, TimeUnit.MINUTES)
            .build()

    companion object {
        private const val WORKER_ID = "TestCertificateRetrievalWorker"

        private val TAG = TestCertificateRetrievalScheduler::class.simpleName!!
    }
}
