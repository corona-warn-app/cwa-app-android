package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RetrievedTestCertificate
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.joda.time.Instant
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
    valueSetsRepository: ValueSetsRepository,
    private val rsaKeyPairGenerator: RSAKeyPairGenerator,
    private val dccStateChecker: DccStateChecker,
    dscRepository: DscRepository
) {

    private val internalData: HotDataFlow<Map<TestCertificateContainerId, TestCertificateContainer>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Eagerly,
    ) {
        storage.load()
            .map {
                TestCertificateContainer(
                    data = it,
                    qrCodeExtractor = qrCodeExtractor
                )
            }.associateBy { it.containerId }
            .also {
                Timber.tag(TAG).v("Restored TestCertificate data: %d items", it.size)
            }
    }

    val certificates: Flow<Set<TestCertificateWrapper>> = combine(
        internalData.data,
        valueSetsRepository.latestTestCertificateValueSets,
        dscRepository.dscData
    ) { certMap, valueSets, _ ->
        certMap.values
            .filter { it.isNotRecycled }
            .map { container ->
                val state = when {
                    container.isCertificateRetrievalPending -> CwaCovidCertificate.State.Invalid()
                    else -> container.testCertificateQRCode?.data?.let {
                        dccStateChecker.checkState(it).first()
                    } ?: CwaCovidCertificate.State.Invalid()
                }

                TestCertificateWrapper(
                    valueSets = valueSets,
                    container = container,
                    certificateState = state,
                )
            }.toSet()
    }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    /**
     * Returns a flow with a set of [TestCertificate] matching the predicate [TestCertificate.isRecycled]
     */
    val recycledCertificates: Flow<Set<TestCertificate>> = internalData.data
        .map { certMap ->
            certMap.values
                .filter { it.isRecycled }
                .mapNotNull { it.toTestCertificate(certificateState = CwaCovidCertificate.State.Recycled) }
                .toSet()
        }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing TestCertificateContainer data.") }
            .drop(1) // Initial emission, restored from storage.
            .onEach { entrySets ->
                val values = entrySets.values
                Timber.tag(TAG).v("TestCertificateContainer data changed: %d items", values.size)
                storage.save(values.map { it.data }.toSet())
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot TestCertificateContainer data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
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

            val rsaKeyPair = try {
                rsaKeyPairGenerator.generate()
            } catch (e: Throwable) {
                throw InvalidTestCertificateException(
                    errorCode = InvalidHealthCertificateException.ErrorCode.RSA_KP_GENERATION_FAILED,
                    cause = e
                )
            }

            val data = when (test.type) {
                CoronaTest.Type.PCR -> PCRCertificateData(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                    labId = test.labId,
                    rsaPublicKey = rsaKeyPair.publicKey,
                    rsaPrivateKey = rsaKeyPair.privateKey,
                )
                CoronaTest.Type.RAPID_ANTIGEN -> RACertificateData(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                    labId = test.labId,
                    rsaPublicKey = rsaKeyPair.publicKey,
                    rsaPrivateKey = rsaKeyPair.privateKey,
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
    ): TestCertificateContainer {
        Timber.tag(TAG).v("registerTestCertificate(qrCode=%s)", qrCode)

        val updatedData = internalData.updateBlocking {

            if (values.any { it.qrCodeHash == qrCode.hash }) {
                Timber.tag(TAG).e("Certificate entry already exists for %s", qrCode)
                throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED)
            }

            val nowUtc = timeStamper.nowUTC

            val data = GenericTestCertificateData(
                identifier = UUID.randomUUID().toString(),
                registeredAt = nowUtc,
                certificateReceivedAt = nowUtc, // Set this as we don't need to retrieve one
                testCertificateQrCode = qrCode.qrCode,
                certificateSeenByUser = false // Just scanned, Should show badge
            )
            val container = TestCertificateContainer(
                data = data,
                qrCodeExtractor = qrCodeExtractor,
            )
            Timber.tag(TAG).d("Adding test certificate entry: %s", container)
            mutate { this[container.containerId] = container }
        }

        // We just registered it, it MUST be available.
        return updatedData.values.single { it.qrCodeHash == qrCode.hash }
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
                    .forEach {
                        remove(TestCertificateContainerId(it.data.identifier))
                        this[it.containerId] = it
                    }
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

            val updated = current.copy(
                data = processor.updateSeenByUser(current.data, true)
            )

            mutate { this[containerId] = updated }
        }
    }

    suspend fun acknowledgeState(containerId: TestCertificateContainerId) {
        // Currently Invalid state supported
        Timber.tag(TAG).d("acknowledgeState(containerId=$containerId)")
        internalData.updateBlocking {
            val current = this[containerId]
            if (current == null) {
                Timber.tag(TAG).w("Can't mark %s state certificate isn't found", containerId)
                return@updateBlocking this
            }

            if (current.isCertificateRetrievalPending) {
                Timber.tag(TAG).w("Can't mark %s state, certificate hasn't been retrieved yet.", containerId)
                return@updateBlocking this
            }

            if (current.testCertificateQRCode == null) {
                Timber.tag(TAG).w("Can't mark %s state, testCertificateQRCode is null.", containerId)
                return@updateBlocking this
            }

            val currentState = dccStateChecker.checkState(current.testCertificateQRCode!!.data).first()

            if (currentState !is CwaCovidCertificate.State.Invalid) {
                Timber.tag(TAG).w("%s is still valid ", containerId)
                return@updateBlocking this
            }

            val lastSeenStateChange = current.data.lastSeenStateChange
            if (currentState == lastSeenStateChange) {
                Timber.tag(TAG).w("State equals last acknowledged state.")
                return@updateBlocking this
            }

            Timber.tag(TAG).d("Acknowledging state change to %s -> %s.", lastSeenStateChange, currentState)

            val updated = current.copy(data = updateLastSeenStateData(current.data, currentState))
            Timber.tag(TAG).d("Updated= %s", updated)

            mutate { this[containerId] = updated }
        }
    }

    suspend fun setNotifiedState(
        containerId: TestCertificateContainerId,
        state: CwaCovidCertificate.State,
        time: Instant
    ) {
        Timber.tag(TAG).d("setNotifiedAboutState(containerId=$containerId, time=$time)")

        internalData.updateBlocking {
            val current = this[containerId]
            if (current == null) {
                Timber.tag(TAG).w("Certificate %s isn't found", containerId)
                return@updateBlocking this
            }

            if (current.isCertificateRetrievalPending) {
                Timber.tag(TAG).w("Certificate %s is pending", containerId)
                return@updateBlocking this
            }

            val isValid = !(state is CwaCovidCertificate.State.Invalid || state is CwaCovidCertificate.State.Blocked)
            if (isValid) {
                Timber.tag(TAG).w("%s is still valid", containerId)
                return@updateBlocking this
            }

            val updated = current.copy(data = updateNotificationTimestamp(current.data, state, time))
            Timber.tag(TAG).d("Updated= %s", updated)
            mutate { this[containerId] = updated }
        }
    }

    /**
     * Move Test certificate to recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun recycleCertificate(containerId: TestCertificateContainerId) {
        Timber.tag(TAG).d("recycleCertificate(containerId=%s)", containerId)
        internalData.updateBlocking {
            val current = this[containerId]
            if (current == null) {
                Timber.tag(TAG).w("recycleCertificate couldn't find %s", containerId)
                return@updateBlocking this
            }

            val updated = current.setRecycled()

            mutate { this[containerId] = updated }
        }
    }

    private fun TestCertificateContainer.setRecycled(): TestCertificateContainer {
        if (isCertificateRetrievalPending) {
            Timber.tag(TAG).w("recycleCertificate couldn't recycle pending TC %s", containerId)
            return this
        }

        return copy(
            data = updateRecycledAt(data, timeStamper.nowUTC)
        )
    }

    /**
     * Restore Test certificate from recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun restoreCertificate(containerId: TestCertificateContainerId) {
        Timber.tag(TAG).d("restoreCertificate(containerId=%s)", containerId)
        internalData.updateBlocking {
            val current = this[containerId]
            if (current == null) {
                Timber.tag(TAG).w("restoreCertificate couldn't find %s", containerId)
                return@updateBlocking this
            }

            if (current.isCertificateRetrievalPending) {
                Timber.tag(TAG).w("restoreCertificate couldn't restore pending TC %s", containerId)
                return@updateBlocking this
            }

            val updated = current.copy(
                data = updateRecycledAt(current.data, null)
            )

            mutate { this[containerId] = updated }
        }
    }

    suspend fun replaceCertificate(
        certificateToReplace: TestCertificateContainerId,
        newCertificateQrCode: TestCertificateQRCode
    ) {
        internalData.updateBlocking {

            val recycledCertificate = this[certificateToReplace]?.setRecycled()

            val nowUtc = timeStamper.nowUTC
            val data = GenericTestCertificateData(
                identifier = UUID.randomUUID().toString(),
                registeredAt = nowUtc,
                certificateReceivedAt = nowUtc, // Set this as we don't need to retrieve one
                testCertificateQrCode = newCertificateQrCode.qrCode,
                certificateSeenByUser = false // Just scanned, Should show badge
            )
            val newCertificate = TestCertificateContainer(
                data = data,
                qrCodeExtractor = qrCodeExtractor,
            )
            Timber.tag(TAG).d("Adding test certificate entry: %s", newCertificate)
            mutate {
                // recylce old
                recycledCertificate?.let {
                    this[certificateToReplace] = it
                }
                // add new
                this[newCertificate.containerId] = newCertificate
            }

        }
    }

    private fun updateLastSeenStateData(
        data: BaseTestCertificateData,
        state: CwaCovidCertificate.State
    ): BaseTestCertificateData {
        return when (data) {
            is PCRCertificateData -> data.copy(
                lastSeenStateChange = state,
                lastSeenStateChangeAt = timeStamper.nowUTC
            )
            is RACertificateData -> data.copy(
                lastSeenStateChange = state,
                lastSeenStateChangeAt = timeStamper.nowUTC
            )
            is GenericTestCertificateData -> data.copy(
                lastSeenStateChange = state,
                lastSeenStateChangeAt = timeStamper.nowUTC
            )
        }
    }

    private fun updateNotificationTimestamp(
        data: BaseTestCertificateData,
        state: CwaCovidCertificate.State,
        now: Instant
    ): BaseTestCertificateData {
        return when (state) {
            is CwaCovidCertificate.State.Blocked -> when (data) {
                is PCRCertificateData -> data.copy(notifiedBlockedAt = now)
                is RACertificateData -> data.copy(notifiedBlockedAt = now)
                is GenericTestCertificateData -> data.copy(notifiedBlockedAt = now)
            }
            is CwaCovidCertificate.State.Invalid -> when (data) {
                is PCRCertificateData -> data.copy(notifiedInvalidAt = now)
                is RACertificateData -> data.copy(notifiedInvalidAt = now)
                is GenericTestCertificateData -> data.copy(notifiedInvalidAt = now)
            }
            // Test certificates notifies only about invalid and blocked states
            else -> throw UnsupportedOperationException("$state is not supported.")
        }
    }

    private fun updateRecycledAt(
        data: BaseTestCertificateData,
        time: Instant?
    ): BaseTestCertificateData {
        return when (data) {
            is PCRCertificateData -> data.copy(recycledAt = time)
            is RACertificateData -> data.copy(recycledAt = time)
            is GenericTestCertificateData -> data.copy(recycledAt = time)
        }
    }

    companion object {
        private val TAG = TestCertificateRepository::class.simpleName!!
    }
}
