package de.rki.coronawarnapp.covidcertificate.test.execution

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.common.ResultScheduler
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateRetrievalScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val certificateRepo: TestCertificateRepository,
    private val testRepo: CoronaTestRepository,
    private val foregroundState: ForegroundState,
) : ResultScheduler(
    workManager = workManager
) {
    private val processedNewCerts = mutableSetOf<String>()

    private val creationTrigger = testRepo.coronaTests
        .map { tests ->
            tests
                .filter { it.isDccSupportedByPoc } // Only those that support it
                .filter { it.isNegative } // Certs only to proof negative state
                .filter { it.isDccConsentGiven && !it.isDccDataSetCreated } // Consent and doesn't exist already?
        }
        .distinctUntilChanged()

    private val refreshTrigger = combine(
        certificateRepo.certificates,
        foregroundState.isInForeground,
    ) { certificates, isForeground ->

        val hasNewCert = certificates.any {
            val isNew = !processedNewCerts.contains(it.identifier)
            if (isNew) processedNewCerts.add(it.identifier)
            isNew
        }

        val hasWorkToDo = certificates.any { it.isCertificateRetrievalPending && !it.isUpdatingData }
        Timber.tag(TAG).v("shouldPollDcc? hasNewCert=$hasNewCert, hasWorkTodo=$hasWorkToDo, foreground=$isForeground")
        (isForeground || hasNewCert) && hasWorkToDo
    }

    fun setup() {
        Timber.tag(TAG).i("setup() - TestCertificateRetrievalScheduler")

        // Create a certificate entry for each viable test that has none
        creationTrigger
            .onEach { testsWithoutCert ->
                Timber.tag(TAG).d("State change: testsWithoutCert=$testsWithoutCert")
                testsWithoutCert.forEach { test ->
                    val cert = certificateRepo.requestCertificate(test)
                    Timber.tag(TAG).v("Certificate was created: %s", cert)
                    testRepo.markDccAsCreated(test.identifier, created = true)
                }
            }
            .catch { Timber.tag(TAG).e(it, "Creation trigger failed.") }
            .launchIn(appScope)

        // For each change to the set of existing certificates, check if we need to refresh/load data
        refreshTrigger
            .onEach { checkCerts ->
                Timber.tag(TAG).d("State change: checkCerts=$checkCerts")
                if (checkCerts) scheduleWorker()
            }
            .catch { Timber.tag(TAG).e(it, "Refresh trigger failed.") }
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
            .setBackoffCriteria(BackoffPolicy.LINEAR, BackgroundConstants.KIND_DELAY, TimeUnit.MINUTES)
            .build()

    companion object {
        private const val WORKER_ID = "TestCertificateRetrievalWorker"

        private val TAG = TestCertificateRetrievalScheduler::class.simpleName!!
    }
}
