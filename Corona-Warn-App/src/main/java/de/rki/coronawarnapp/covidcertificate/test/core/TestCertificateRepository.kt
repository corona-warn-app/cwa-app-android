package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RetrievedTestCertificate
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val processor: TestCertificateProcessor,
    private val timeStamper: TimeStamper,
    private val valueSetsRepository: ValueSetsRepository,
) {

    private val internalData: HotDataFlow<Map<TestCertificateContainerId, TestCertificateContainer>> = HotDataFlow(
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
            .map { it.containerId to it }
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
     * Automation via [de.rki.coronawarnapp.covidcertificate.test.core.execution.TestCertificateRetrievalScheduler] will kick in.
     *
     * Throws an exception if there already is a test certificate entry for this test
     * or this is not a valid test (no consent, not supported by PoC).
     */
    suspend fun requestCertificate(test: CoronaTest): TestCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(test.identifier=%s)", test.identifier)

        val newData = internalData.updateBlocking {

            val matchesExisting = values
                .map { it.data }
                .filterIsInstance<RetrievedTestCertificate>()
                .any { it.registrationToken == test.registrationToken }

            if (matchesExisting) {
                Timber.tag(TAG).e("Certificate entry already exists for %s", test.identifier)
                throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED)
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
                    labId = test.labId
                )
                CoronaTest.Type.RAPID_ANTIGEN -> RACertificateData(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                    labId = test.labId
                )
            }
            val container = TestCertificateContainer(
                data = data,
                qrCodeExtractor = qrCodeExtractor,
            )
            Timber.tag(TAG).d("Adding test certificate entry: %s", container)
            mutate { this[container.containerId] = container }
        }

        return newData.values.single {
            it.data is RetrievedTestCertificate && it.data.registrationToken == test.registrationToken
        }
    }

    suspend fun registerCertificate(
        qrCode: TestCertificateQRCode
    ): TestCertificate {
        Timber.tag(TAG).v("registerTestCertificate(qrCode=%s)", qrCode)

        val updatedData = internalData.updateBlocking {

            if (values.any { it.certificateId == qrCode.uniqueCertificateIdentifier }) {
                Timber.tag(TAG).e("Certificate entry already exists for %s", qrCode)
                throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED)
            }

            val nowUtc = timeStamper.nowUTC

            val data = GenericTestCertificateData(
                identifier = UUID.randomUUID().toString(),
                registeredAt = nowUtc,
                certificateReceivedAt = nowUtc,
                testCertificateQrCode = qrCode.qrCode
            )
            val container = TestCertificateContainer(
                data = data,
                qrCodeExtractor = qrCodeExtractor,
            )
            Timber.tag(TAG).d("Adding test certificate entry: %s", container)
            mutate { this[container.containerId] = container }
        }

        // We just registered it, it MUST be available.
        return updatedData.values
            .single { it.certificateId == qrCode.uniqueCertificateIdentifier }
            .toTestCertificate(
                valueSetsRepository.latestTestCertificateValueSets.first()
            )!!
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
    @Suppress("ComplexMethod")
    suspend fun refresh(containerId: TestCertificateContainerId? = null): Set<RefreshResult> {
        Timber.tag(TAG).d("refresh(containerId=%s)", containerId)

        val refreshCallResults = mutableMapOf<TestCertificateContainerId, RefreshResult>()

        val workedOnIds = mutableSetOf<TestCertificateContainerId>()

        internalData.updateBlocking {
            val toRefresh = values
                .filter { it.containerId == containerId || containerId == null } // Targets of our refresh
                .filter { it.data is RetrievedTestCertificate } // Can only update retrieved certificates
                .filter { !it.isUpdatingData && it.isCertificateRetrievalPending } // Those that need refreshing

            mutate {
                toRefresh.forEach {
                    workedOnIds.add(it.containerId)
                    this[it.containerId] = it.copy(isUpdatingData = true)
                }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for unregistered public keys.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.containerId) } // Refresh targets
                .mapNotNull { cert ->
                    if (cert.data !is RetrievedTestCertificate) return@mapNotNull null
                    if (cert.data.isPublicKeyRegistered) return@mapNotNull null

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
                refreshCallResults[it.certificateContainer.containerId] = it
            }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificateContainer }
                    .forEach { this[it.containerId] = it }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for pending certificates.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.containerId) } // Refresh targets
                .mapNotNull { cert ->
                    if (cert.data !is RetrievedTestCertificate) return@mapNotNull null

                    if (!cert.data.isPublicKeyRegistered) return@mapNotNull null
                    if (!cert.isCertificateRetrievalPending) return@mapNotNull null

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
                refreshCallResults[it.certificateContainer.containerId] = it
            }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificateContainer }
                    .forEach { this[it.containerId] = it }
            }
        }

        internalData.updateBlocking {
            mutate {
                values
                    .filter { workedOnIds.contains(it.containerId) }
                    .forEach { this[it.containerId] = it.copy(isUpdatingData = false) }
            }
        }

        return refreshCallResults.values.toSet()
    }

    /**
     * [deleteCertificate] does not throw an exception, if the deletion target already does not exist.
     */
    suspend fun deleteCertificate(containerId: TestCertificateContainerId): TestCertificateContainer? {
        Timber.tag(TAG).d("deleteCertificate(containerId=%s)", containerId)

        var deletedCertificate: TestCertificateContainer? = null

        internalData.updateBlocking {
            mutate {
                deletedCertificate = remove(containerId)
            }
        }

        return deletedCertificate?.also {
            Timber.tag(TAG).i("Deleted: %s", containerId)
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        internalData.updateBlocking { emptyMap() }
    }

    suspend fun markCertificateAsSeenByUser(containerId: TestCertificateContainerId) {
        Timber.tag(TAG).d("markCertificateSeenByUser(containerId=%s)", containerId)

        internalData.updateBlocking {
            val current = this[containerId]
            if (current == null) {
                Timber.tag(TAG).w("Can't mark %s as seen, it doesn't exist, racecondition?", containerId)
                return@updateBlocking this
            }

            if (current.isCertificateRetrievalPending) {
                Timber.tag(TAG).w("Can't mark %s as seen, certificate has not been retrieved yet.", containerId)
                return@updateBlocking this
            }

            if (current.data !is RetrievedTestCertificate) {
                Timber.tag(TAG).w("%s is not a retrieved certificate, so it was immediately available.", containerId)
                return@updateBlocking this
            }

            val updated = current.copy(
                data = processor.updateSeenByUser(current.data, true)
            )

            mutate { this[containerId] = updated }
        }
    }

    companion object {
        private val TAG = TestCertificateRepository::class.simpleName!!
    }
}
