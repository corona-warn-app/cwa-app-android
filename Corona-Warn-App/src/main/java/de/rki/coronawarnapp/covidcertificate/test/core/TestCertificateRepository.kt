package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.core.storage.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.RACertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateProcessor
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.ValueSetsRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("LongParameterList")
class TestCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: TestCertificateStorage,
    private val qrCodeExtractor: TestCertificateQRCodeExtractor,
    private val processor: TestCertificateProcessor,
    valueSetsRepository: ValueSetsRepository,
) {

    private val internalData: HotDataFlow<Map<TestCertificateIdentifier, TestCertificateContainer>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Eagerly,
    ) {
        storage.testCertificates
            .map {
                TestCertificateContainer(
                    data = it,
                    qrCodeExtractor = qrCodeExtractor
                )
            }
            .map { it.identifier to it }
            .toMap().also {
                Timber.tag(TAG).v("Restored TestCertificate data: %s", it)
            }
    }

    val certificates: Flow<Set<TestCertificateWrapper>> = combine(
        internalData.data,
        valueSetsRepository.latestTestCertificateValueSets
    ) { certMap, valueSets ->
        certMap.values.map { container ->
            TestCertificateWrapper(
                valueSets = valueSets,
                container = container,
            )
        }.toSet()
    }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing TestCertificateContainer data.") }
            .onEach { entrySets ->
                val values = entrySets.values
                Timber.tag(TAG).v("TestCertificateContainer data changed: %s", values)
                storage.testCertificates = values.map { it.data }.toSet()
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot TestCertificateContainer data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.Default)
    }

    /**
     * Will create a new test certificate entry.
     * Automation via [de.rki.coronawarnapp.coronatest.type.common.TestCertificateRetrievalScheduler] will kick in.
     *
     * Throws an exception if there already is a test certificate entry for this test
     * or this is not a valid test (no consent, not supported by PoC).
     */
    suspend fun requestCertificate(test: CoronaTest): TestCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(test.identifier=%s)", test.identifier)

        val newData = internalData.updateBlocking {
            if (values.any { it.registrationToken == test.registrationToken }) {
                Timber.tag(TAG).e("Certificate entry already exists for %s", test.identifier)
                throw IllegalArgumentException("A certificate was already created for this ${test.identifier}")
            }
            if (!test.isDccSupportedByPoc) {
                throw IllegalArgumentException("DCC is not supported by PoC for this test: ${test.identifier}")
            }
            if (!test.isDccConsentGiven) {
                throw IllegalArgumentException("DCC was not given for this test: ${test.identifier}")
            }

            val identifier = UUID.randomUUID().toString()

            val data = when (test.type) {
                CoronaTest.Type.PCR -> PCRCertificateData(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                )
                CoronaTest.Type.RAPID_ANTIGEN -> RACertificateData(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                )
            }
            val container = TestCertificateContainer(
                data = data,
                qrCodeExtractor = qrCodeExtractor,
            )
            Timber.tag(TAG).d("Adding test certificate entry: %s", container)
            mutate { this[container.identifier] = container }
        }

        return newData.values.single { it.registrationToken == test.registrationToken }
    }

    /**
     * If [error] is NULL, then [certificateContainer] will be the refreshed entry.
     * If [error] is not NULL, then [certificateContainer] is the latest version before the exception occured.
     * Due to refresh being a multiple process, some steps can successed, while others fail.
     */
    data class RefreshResult(
        val certificateContainer: TestCertificateContainer,
        val error: Exception? = null,
    )

    /**
     * The refresh call checks each certificate entry for public keys and certificate state.
     * It will be triggered via TestCertificateRetrievalScheduler.
     * After requestCertificate, calling refresh often enough should yield a certificate eventually.
     *
     * This returns a set of [RefreshResult], one for each refreshed test certificate entry.
     * If you specify  an identifier, then the set will only contain a single element.
     *
     * [refresh] itself will NOT throw an exception.
     */
    suspend fun refresh(identifier: TestCertificateIdentifier? = null): Set<RefreshResult> {
        Timber.tag(TAG).d("refresh(identifier=%s)", identifier)

        val refreshCallResults = mutableMapOf<TestCertificateIdentifier, RefreshResult>()

        val workedOnIds = mutableSetOf<TestCertificateIdentifier>()

        internalData.updateBlocking {
            val toRefresh = values
                .filter { it.identifier == identifier || identifier == null } // Targets of our refresh
                .filter { !it.isUpdatingData && it.isCertificateRetrievalPending } // Those that need refreshing

            mutate {
                toRefresh.forEach {
                    workedOnIds.add(it.identifier)
                    this[it.identifier] = it.copy(isUpdatingData = false)
                }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for unregistered public keys.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.identifier) } // Refresh targets
                .filter { !it.isPublicKeyRegistered } // Targets of this step
                .map { cert ->
                    withContext(dispatcherProvider.IO) {
                        try {
                            val updatedData = processor.registerPublicKey(cert.data)
                            RefreshResult(cert.copy(data = updatedData))
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Failed to register public key for %s", cert)
                            RefreshResult(cert, e)
                        }
                    }
                }

            refreshedCerts.forEach {
                refreshCallResults[it.certificateContainer.identifier] = it
            }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificateContainer }
                    .forEach { this[it.identifier] = it }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for pending certificates.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.identifier) } // Refresh targets
                .filter { it.isPublicKeyRegistered && it.isCertificateRetrievalPending } // Targets of this step
                .map { cert ->
                    withContext(dispatcherProvider.IO) {
                        try {
                            val updatedData = processor.obtainCertificate(cert.data)
                            RefreshResult(cert.copy(data = updatedData))
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Failed to retrieve certificate components for %s", cert)
                            RefreshResult(cert, e)
                        }
                    }
                }

            refreshedCerts.forEach {
                refreshCallResults[it.certificateContainer.identifier] = it
            }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificateContainer }
                    .forEach { this[it.identifier] = it }
            }
        }

        internalData.updateBlocking {
            val certs = values.filter { workedOnIds.contains(it.identifier) }

            mutate {
                certs.forEach {
                    this[it.identifier] = it.copy(isUpdatingData = false)
                }
            }
        }

        return refreshCallResults.values.toSet()
    }

    /**
     * [deleteCertificate] does not throw an exception, if the deletion target already does not exist.
     */
    suspend fun deleteCertificate(identifier: TestCertificateIdentifier) {
        Timber.tag(TAG).d("deleteTestCertificate(identifier=%s)", identifier)
        internalData.updateBlocking {
            mutate {
                remove(identifier)
            }
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        internalData.updateBlocking { emptyMap() }
    }

    companion object {
        private val TAG = TestCertificateRepository::class.simpleName!!
    }
}
