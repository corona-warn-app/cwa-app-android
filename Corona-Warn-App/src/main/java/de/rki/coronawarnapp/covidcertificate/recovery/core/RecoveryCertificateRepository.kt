package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidRecoveryCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasures
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasuresObserver
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.StoredRecoveryCertificateData
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.flow.combine
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.mutate
import de.rki.coronawarnapp.util.reset.Resettable
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
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateRepository @Inject constructor(
    private val valueSetsRepository: ValueSetsRepository,
    dispatcherProvider: DispatcherProvider,
    @AppScope private val appScope: CoroutineScope,
    private val timeStamper: TimeStamper,
    private val dccState: DccStateChecker,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val storage: RecoveryCertificateStorage,
    private val dccValidityMeasuresObserver: DccValidityMeasuresObserver
) : Resettable {

    private val internalData: HotDataFlow<Map<RecoveryCertificateContainerId, RecoveryCertificateContainer>> =
        HotDataFlow(
            loggingTag = TAG,
            scope = appScope + dispatcherProvider.Default,
            sharingBehavior = SharingStarted.Lazily,
        ) {
            storage.load()
                .map { recoveryCertificate ->
                    RecoveryCertificateContainer(
                        data = recoveryCertificate,
                        qrCodeExtractor = qrCodeExtractor
                    )
                }.associateBy { it.containerId }
                .also { Timber.tag(TAG).v("Restored recovery certificate data: %d items", it.size) }
        }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing RecoveryCertificateContainer data.") }
            .drop(1) // Initial emission, restored from storage.
            .onEach { recoveryCertificates ->
                Timber.tag(TAG).v("Recovery Certificate data changed: %d items", recoveryCertificates.size)
                storage.save(recoveryCertificates.values.map { it.data }.toSet())
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot recovery certificate data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    /**
     * All [RecoveryCertificate] in the app whether recycled or not
     */
    val allCertificates: Flow<RecoveryCertificatesHolder> = combine(
        internalData.data,
        valueSetsRepository.latestVaccinationValueSets,
        dccValidityMeasuresObserver.dccValidityMeasures
    ) { certMap, valueSets, dccValidityMeasures ->
        val certificates = mutableSetOf<RecoveryCertificateWrapper>()
        val recycledCertificates = mutableSetOf<RecoveryCertificate>()

        certMap.values.forEach {
            when {
                it.isNotRecycled -> it.toRecoveryCertificateWrapper(valueSets, dccValidityMeasures)
                    ?.let { rc -> certificates += rc }
                it.isRecycled -> it.toRecoveryCertificateOrNull(valueSets, CwaCovidCertificate.State.Recycled)
                    ?.let { rc -> recycledCertificates += rc }
            }
        }

        RecoveryCertificatesHolder(
            certificates = certificates,
            recycledCertificates = recycledCertificates
        )
    }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    fun findCertificateDetails(containerId: RecoveryCertificateContainerId): Flow<RecoveryCertificate?> =
        internalData.data.map { map ->
            map[containerId].takeIf { it?.isNotRecycled == true }?.toRecoveryCertificateWrapper(
                valueSetsRepository.latestVaccinationValueSets.first(),
                dccValidityMeasuresObserver.dccValidityMeasures.first()
            )?.recoveryCertificate
        }

    /**
     * Returns a flow with a set of [RecoveryCertificate] matching the predicate [RecoveryCertificate.isNotRecycled]
     */
    val certificates: Flow<Set<RecoveryCertificateWrapper>> = allCertificates
        .map { it.certificates }
        .shareLatest(scope = appScope)

    /**
     * Returns a flow with a set of [RecoveryCertificate] matching the predicate [RecoveryCertificate.isRecycled]
     */
    val recycledCertificates: Flow<Set<RecoveryCertificate>> = allCertificates
        .map { it.recycledCertificates }
        .shareLatest(scope = appScope)

    @Throws(InvalidRecoveryCertificateException::class)
    suspend fun registerCertificate(qrCode: RecoveryCertificateQRCode): RecoveryCertificateContainer {
        Timber.tag(TAG).d("registerCertificate(qrCode=%s)", qrCode)
        val newContainer = qrCode.toContainer()
        internalData.updateBlocking {
            if (values.any { it.qrCodeHash == newContainer.qrCodeHash }) {
                throw InvalidRecoveryCertificateException(
                    InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
                )
            }
            mutate {
                this[newContainer.containerId] = newContainer
            }
        }
        return newContainer
    }

    private fun RecoveryCertificateQRCode.toContainer() = RecoveryCertificateContainer(
        data = StoredRecoveryCertificateData(
            recoveryCertificateQrCode = qrCode,
            certificateSeenByUser = false
        ),
        qrCodeExtractor = qrCodeExtractor,
        isUpdatingData = false
    )

    suspend fun deleteCertificate(containerId: RecoveryCertificateContainerId) {
        Timber.tag(TAG).d("deleteCertificate(containerId=%s)", containerId)
        internalData.updateBlocking {
            mutate { remove(containerId) }
        }
    }

    override suspend fun reset() {
        Timber.tag(TAG).w("Clearing recovery certificate data.")
        internalData.updateBlocking {
            Timber.tag(TAG).v("Deleting: %d items", this.size)
            emptyMap()
        }
    }

    suspend fun setNotifiedState(
        containerId: RecoveryCertificateContainerId,
        state: CwaCovidCertificate.State,
        time: Instant?,
    ) {
        Timber.tag(TAG).d("setNotifiedAboutState(containerId=$containerId, time=$time)")
        internalData.updateBlocking {
            val toUpdate = this[containerId]
            if (toUpdate == null) {
                Timber.tag(TAG).w("Couldn't find %s", containerId)
                return@updateBlocking this
            }

            val newData = when (state) {
                is Invalid -> toUpdate.data.copy(notifiedInvalidAt = time)
                is Blocked -> toUpdate.data.copy(notifiedBlockedAt = time)
                is Revoked -> toUpdate.data.copy(notifiedRevokedAt = time)
                else -> throw UnsupportedOperationException("$state is not supported.")
            }

            Timber.tag(TAG).d("Updated %s", containerId)
            mutate {
                this[containerId] = toUpdate.copy(data = newData)
            }
        }
    }

    suspend fun acknowledgeState(containerId: RecoveryCertificateContainerId) {
        Timber.tag(TAG).d("acknowledgeStateChange(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdate = this[containerId]
            if (toUpdate == null) {
                Timber.tag(TAG).w("Couldn't find %s", containerId)
                return@updateBlocking this
            }

            val currentState = dccState(
                dccData = toUpdate.certificateData,
                qrCodeHash = toUpdate.qrCodeHash,
                dccValidityMeasures = dccValidityMeasuresObserver.dccValidityMeasures()
            )

            if (currentState == toUpdate.data.lastSeenStateChange) {
                Timber.tag(TAG).w("State equals last acknowledged state.")
                return@updateBlocking this
            }

            Timber.tag(TAG)
                .d("Acknowledging state change to %s -> %s.", toUpdate.data.lastSeenStateChange, currentState)
            val newData = toUpdate.data.copy(
                lastSeenStateChange = currentState,
                lastSeenStateChangeAt = timeStamper.nowUTC,
            )

            Timber.tag(TAG).d("Updated %s", containerId)
            mutate {
                this[containerId] = toUpdate.copy(data = newData)
            }
        }
    }

    suspend fun markAsSeenByUser(containerId: RecoveryCertificateContainerId) {
        Timber.tag(TAG).d("markAsSeenByUser(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdate = this[containerId]
            if (toUpdate == null) {
                Timber.tag(TAG).w("markAsSeenByUser Couldn't find %s", containerId)
                return@updateBlocking this
            }

            mutate {
                this[containerId] = toUpdate.markAsSeenByUser()
            }
        }
    }

    /**
     * Move Recovery certificate to recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun recycleCertificate(containerId: RecoveryCertificateContainerId) {
        Timber.tag(TAG).d("recycleCertificate(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdate = this[containerId]
            if (toUpdate == null) {
                Timber.tag(TAG).w("recycleCertificate couldn't find %s", containerId)
                return@updateBlocking this
            }

            mutate {
                this[containerId] = toUpdate.setRecycled(true)
            }
        }
    }

    /**
     * Restore Recovery certificate from recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun restoreCertificate(containerId: RecoveryCertificateContainerId) {
        Timber.tag(TAG).d("restoreCertificate(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdate = this[containerId]
            if (toUpdate == null) {
                Timber.tag(TAG).w("restoreCertificate couldn't find %s", containerId)
                return@updateBlocking this
            }

            mutate {
                this[containerId] = toUpdate.setRecycled(false)
            }
        }
    }

    private fun RecoveryCertificateContainer.setRecycled(value: Boolean): RecoveryCertificateContainer {
        return copy(data = data.copy(recycledAt = if (value) timeStamper.nowUTC else null)).also {
            Timber.tag(TAG).d("recycleCertificate %s %s", value, it.containerId)
        }
    }

    private fun RecoveryCertificateContainer.markAsSeenByUser(): RecoveryCertificateContainer {
        return copy(data = data.copy(certificateSeenByUser = true)).also {
            Timber.tag(TAG).d("markAsSeenByUser %s", it.containerId)
        }
    }

    private fun RecoveryCertificateContainer.toRecoveryCertificateOrNull(
        valueSet: VaccinationValueSets?,
        certificateState: CwaCovidCertificate.State
    ): RecoveryCertificate? {
        try {
            return toRecoveryCertificate(valueSet, certificateState)
            // read value from dcc data to throw an exception early if the DccQrCodeExtractor is not able to parse
            // the certificate
        } catch (e: Exception) {
            Timber.e(e, "Creating RecoveryCertificate failed")
        }
        return null
    }

    private suspend fun RecoveryCertificateContainer.toRecoveryCertificateWrapper(
        valueSets: VaccinationValueSets,
        dccValidityMeasures: DccValidityMeasures
    ): RecoveryCertificateWrapper? {
        try {
            val state = dccState(
                dccData = certificateData,
                qrCodeHash = qrCodeHash,
                dccValidityMeasures = dccValidityMeasures
            )

            return RecoveryCertificateWrapper(
                valueSets = valueSets,
                container = this,
                certificateState = state
            )
        } catch (e: Exception) {
            Timber.e(e, "Creating RecoveryCertificateWrapper failed")
        }
        return null
    }

    companion object {
        private val TAG = RecoveryCertificateRepository::class.simpleName!!
    }
}
