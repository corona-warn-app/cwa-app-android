package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.storage.TestCertificateStorage
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.TestCertificate
import de.rki.coronawarnapp.coronatest.type.TestCertificateIdentifier
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: TestCertificateStorage,
) {

    private val internalData: HotDataFlow<Map<TestCertificateIdentifier, TestCertificate>> = HotDataFlow(
        loggingTag = CoronaTestRepository.TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
    ) {
        storage.testCertificates.map { it.identifier to it }.toMap().also {
            Timber.tag(CoronaTestRepository.TAG).v("Restored TestCertificate data: %s", it)
        }
    }

    val coronaTests: Flow<Set<TestCertificate>> = internalData.data.map { it.values.toSet() }

    init {
        internalData.data
            .onStart { Timber.tag(CoronaTestRepository.TAG).d("Observing test certificate data.") }
            .onEach {
                Timber.tag(CoronaTestRepository.TAG).v("TestCertificate data changed: %s", it)
                storage.testCertificates = it.values.toSet()
            }
            .catch {
                it.reportProblem(CoronaTestRepository.TAG, "Failed to snapshot TestCertificate data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    suspend fun createDccForTest(test: CoronaTest): TestCertificate {
        Timber.tag(TAG).d("createDccForTest(test.identifier=%s)", test.identifier)
        throw NotImplementedError()
    }

    suspend fun deleteTestCertificate(identifier: TestCertificateIdentifier) {
        Timber.tag(TAG).d("deleteTestCertificate(identifier=%s)", identifier)
        throw NotImplementedError()
    }

    companion object {
        val TAG = TestCertificateRepository::class.simpleName!!
    }
}
