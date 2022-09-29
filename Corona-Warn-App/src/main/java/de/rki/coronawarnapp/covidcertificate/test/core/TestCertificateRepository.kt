package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.isScreenedCert
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasures
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasuresObserver
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RetrievedTestCertificate
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.mutate
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.time.Instant
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("LongParameterList")
class TestCertificateRepository @Inject constructor(
    private val valueSetsRepository: ValueSetsRepository,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: TestCertificateStorage,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val processor: TestCertificateProcessor,
    private val timeStamper: TimeStamper,
    private val rsaKeyPairGenerator: RSAKeyPairGenerator,
    private val dccState: DccStateChecker,
    private val dccValidityMeasuresObserver: DccValidityMeasuresObserver
) : Resettable {

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
     * All [TestCertificate] in the app whether recycled or not
     */
    val allCertificates: Flow<TestCertificatesHolder> = combine(
        internalData.data,
        valueSetsRepository.latestTestCertificateValueSets,
        dccValidityMeasuresObserver.dccValidityMeasures
    ) { certMap, valueSets, dccValidityMeasures ->
        val certificates = mutableSetOf<TestCertificateWrapper>()
        val recycledCertificates = mutableSetOf<TestCertificate?>()

        certMap.values.forEach {
            when {
                it.isNotRecycled -> it.toTestCertificateWrapper(valueSets, dccValidityMeasures)
                    ?.let { tc -> certificates += tc }
                it.isRecycled -> it.toTestCertificateOrNull(valueSets, CwaCovidCertificate.State.Recycled)
                    ?.let { tc -> recycledCertificates += tc }
            }
        }

        TestCertificatesHolder(
            certificates = certificates,
            recycledCertificates = recycledCertificates.filterNotNull().toSet()
        )
    }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    fun findCertificateDetails(containerId: TestCertificateContainerId): Flow<TestCertificate?> =
        internalData.data.map { map ->
            map[containerId].takeIf { it?.isNotRecycled == true }?.toTestCertificateWrapper(
                valueSetsRepository.latestTestCertificateValueSets.first(),
                dccValidityMeasuresObserver.dccValidityMeasures.first()
            )?.testCertificate
        }

    /**
     * Returns a flow with a set of [TestCertificate] matching the predicate [TestCertificate.isNotRecycled]
     */
    val certificates: Flow<Set<TestCertificateWrapper>> = allCertificates
        .map { it.certificates }
        .shareLatest(scope = appScope)

    /**
     * Returns a flow with a set of [TestCertificate] matching the predicate [TestCertificate.isRecycled]
     */
    val recycledCertificates: Flow<Set<TestCertificate>> = allCertificates
        .map { it.recycledCertificates }
        .shareLatest(scope = appScope)

    /**
     * Will create a new test certificate entry.
     * Automation via [de.rki.coronawarnapp.covidcertificate.test.core.execution.TestCertificateRetrievalScheduler] will kick in.
     *
     * Throws an exception if there already is a test certificate entry for this test
     * or this is not a valid test (no consent, not supported by PoC).
     */
    suspend fun requestCertificate(test: BaseCoronaTest): TestCertificateContainer {
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
                BaseCoronaTest.Type.PCR -> PCRCertificateData(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                    labId = test.labId,
                    rsaPublicKey = rsaKeyPair.publicKey,
                    rsaPrivateKey = rsaKeyPair.privateKey,
                )
                BaseCoronaTest.Type.RAPID_ANTIGEN -> RACertificateData(
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
            val container = qrCode.createContainer()
            Timber.tag(TAG).d("Adding test certificate entry: %s", container.containerId)
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
                            Timber.tag(TAG).e(
                                e,
                                "Failed to retrieve certificate components for %s",
                                cert.containerId
                            )
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
     * This method should be called when the test certificate worker is canceled. This could happen for example when the
     * device is disconnected from the internet, work constraints are not met anymore and work manager cancels update
     * job. It set test certificates back to not updating state to prevent endless loading indicators in the UI.
     */
    suspend fun refreshCleanup() {
        Timber.tag(TAG).d("refreshCleanup()")
        internalData.updateBlocking {
            mutate {
                // filter
                values.forEach { this[it.containerId] = it.copy(isUpdatingData = false) }
            }
        }
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

    override suspend fun reset() {
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

            val currentState = dccState(
                dccData = current.testCertificateQRCode!!.data,
                qrCodeHash = current.qrCodeHash,
                dccValidityMeasures = dccValidityMeasuresObserver.dccValidityMeasures()
            )

            if (!isScreenedCert(currentState)) {
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

            val isValid = !isScreenedCert(state)
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
            is Blocked -> when (data) {
                is PCRCertificateData -> data.copy(notifiedBlockedAt = now)
                is RACertificateData -> data.copy(notifiedBlockedAt = now)
                is GenericTestCertificateData -> data.copy(notifiedBlockedAt = now)
            }

            is Revoked -> when (data) {
                is PCRCertificateData -> data.copy(notifiedRevokedAt = now)
                is RACertificateData -> data.copy(notifiedRevokedAt = now)
                is GenericTestCertificateData -> data.copy(notifiedRevokedAt = now)
            }

            is Invalid -> when (data) {
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

    private fun TestCertificateQRCode.createContainer(
        nowUtc: Instant = timeStamper.nowUTC
    ): TestCertificateContainer {
        val data = GenericTestCertificateData(
            identifier = UUID.randomUUID().toString(),
            registeredAt = nowUtc,
            certificateReceivedAt = nowUtc, // Set this as we don't need to retrieve one
            testCertificateQrCode = qrCode,
            certificateSeenByUser = false // Newly added, should show badge
        )
        return TestCertificateContainer(
            data = data,
            qrCodeExtractor = qrCodeExtractor,
        )
    }

    private fun TestCertificateContainer.toTestCertificateOrNull(
        valueSet: TestCertificateValueSets?,
        certificateState: CwaCovidCertificate.State
    ): TestCertificate? {
        try {
            return toTestCertificate(valueSet, certificateState)
            // read value from dcc data to throw an exception early if the DccQrCodeExtractor is not able to parse
            // the certificate
        } catch (e: Exception) {
            Timber.e(e, "Creating TestCertificate failed")
        }
        return null
    }

    private suspend fun TestCertificateContainer.toTestCertificateWrapper(
        valueSets: TestCertificateValueSets,
        dccValidityMeasures: DccValidityMeasures
    ): TestCertificateWrapper? {
        try {
            val state = when {
                isCertificateRetrievalPending -> Invalid()
                else -> testCertificateQRCode?.let {
                    dccState(
                        dccData = it.data,
                        qrCodeHash = it.qrCode.toSHA256(),
                        dccValidityMeasures = dccValidityMeasures
                    )
                } ?: Invalid()
            }

            return TestCertificateWrapper(
                valueSets = valueSets,
                container = this,
                certificateState = state,
            )
        } catch (e: Exception) {
            Timber.e(e, "Creating TestCertificateWrapper failed")
        }
        return null
    }

    companion object {
        private val TAG = TestCertificateRepository::class.simpleName!!
    }
}
