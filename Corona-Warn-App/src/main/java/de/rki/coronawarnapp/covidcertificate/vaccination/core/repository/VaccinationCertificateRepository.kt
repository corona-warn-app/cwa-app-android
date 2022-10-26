package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasures
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasuresObserver
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationMigration
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationCertificateContainer
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.toVaccinationContainer
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
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
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("LongParameterList")
class VaccinationCertificateRepository @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val valueSetsRepository: ValueSetsRepository,
    private val timeStamper: TimeStamper,
    private val storage: VaccinationStorage,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val dccState: DccStateChecker,
    private val vaccinationMigration: VaccinationMigration,
    @AppScope private val appScope: CoroutineScope,
    private val dccValidityMeasuresObserver: DccValidityMeasuresObserver
) : Resettable {

    private val internalData: HotDataFlow<Map<VaccinationCertificateContainerId, VaccinationCertificateContainer>> =
        HotDataFlow(
            loggingTag = TAG,
            scope = appScope + dispatcherProvider.Default,
            sharingBehavior = SharingStarted.Lazily,
        ) {
            (storage.load() + vaccinationMigration.doMigration())
                .map {
                    VaccinationCertificateContainer(
                        data = it,
                        qrCodeExtractor = qrCodeExtractor
                    )
                }.associateBy {
                    it.containerId
                }
                .also { Timber.tag(TAG).v("Restored vaccination data, %d items", it.size) }
        }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing VaccinationContainer data.") }
            .drop(1) // Initial emission, restored from storage.
            .onEach { certificates ->
                Timber.tag(TAG).v("Vaccination data changed, %d items", certificates.size)
                storage.save(certificates.values.map { it.data }.toSet())
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot vaccination data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    /**
     * All [VaccinationCertificate] in the app whether recycled or not
     */
    val allCertificates: Flow<VaccinationCertificatesHolder> = combine(
        internalData.data,
        valueSetsRepository.latestVaccinationValueSets,
        dccValidityMeasuresObserver.dccValidityMeasures
    ) { certMap, valueSets, dccValidityMeasures ->
        val certificates = mutableSetOf<VaccinationCertificateWrapper>()
        val recycledCertificates = mutableSetOf<VaccinationCertificate>()

        certMap.values.forEach {
            when {
                it.isNotRecycled -> it.toVaccinationCertificateWrapper(valueSets, dccValidityMeasures)
                    ?.let { vc -> certificates += vc }
                it.isRecycled -> it.toVaccinationCertificateOrNull(valueSets, CwaCovidCertificate.State.Recycled)
                    ?.let { vc -> recycledCertificates += vc }
            }
        }

        VaccinationCertificatesHolder(
            certificates = certificates,
            recycledCertificates = recycledCertificates
        )
    }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    fun findCertificateDetails(containerId: VaccinationCertificateContainerId): Flow<VaccinationCertificate?> =
        internalData.data.map { map ->
            map[containerId].takeIf { it?.isNotRecycled == true }?.toVaccinationCertificateWrapper(
                valueSetsRepository.latestVaccinationValueSets.first(),
                dccValidityMeasuresObserver.dccValidityMeasures.first()
            )?.vaccinationCertificate
        }

    /**
     * Returns a flow with a set of [VaccinationCertificate] matching the predicate
     * [VaccinationCertificate.isNotRecycled]
     */
    val certificates: Flow<Set<VaccinationCertificateWrapper>> = allCertificates
        .map { it.certificates }
        .shareLatest(scope = appScope)

    /**
     * Returns a flow with a set of [VaccinationCertificate] matching the predicate [VaccinationCertificate.isRecycled]
     */
    val recycledCertificates: Flow<Set<VaccinationCertificate>> = allCertificates
        .map { it.recycledCertificates }
        .shareLatest(scope = appScope)

    suspend fun registerCertificate(
        qrCode: VaccinationCertificateQRCode,
    ): VaccinationCertificateContainer {
        Timber.tag(TAG).v("registerVaccination(qrCode=%s)", qrCode)

        val newCertificate = qrCode.createContainer()

        internalData.updateBlocking {
            if (values.any { it.qrCodeHash == qrCode.hash }) {
                Timber.tag(TAG).e("Certificate is already registered: %s", qrCode.hash)
                throw InvalidVaccinationCertificateException(ALREADY_REGISTERED)
            }
            mutate { this[newCertificate.containerId] = newCertificate }
        }

        return newCertificate
    }

    override suspend fun reset() {
        Timber.tag(TAG).w("Clearing vaccination data.")
        internalData.updateBlocking {
            Timber.tag(TAG).v("Deleting %d items", this.size)
            emptyMap()
        }
    }

    suspend fun deleteCertificate(containerId: VaccinationCertificateContainerId) {
        Timber.tag(TAG).w("deleteCertificate(containerId=%s)", containerId)
        internalData.updateBlocking {
            mutate { remove(containerId) }
        }
    }

    suspend fun setNotifiedState(
        containerId: VaccinationCertificateContainerId,
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

    suspend fun acknowledgeState(containerId: VaccinationCertificateContainerId) {
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

    suspend fun markAsSeenByUser(containerId: VaccinationCertificateContainerId) {
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
     * Move Vaccination certificate to recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun recycleCertificate(containerId: VaccinationCertificateContainerId) {
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
     * Restore Vaccination certificate from recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun restoreCertificate(containerId: VaccinationCertificateContainerId) {
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

    private fun VaccinationCertificateContainer.setRecycled(value: Boolean): VaccinationCertificateContainer {
        return copy(data = data.copy(recycledAt = if (value) timeStamper.nowUTC else null)).also {
            Timber.tag(TAG).d("recycleCertificate %s %s", value, it.containerId)
        }
    }

    private fun VaccinationCertificateContainer.markAsSeenByUser(): VaccinationCertificateContainer {
        return copy(data = data.copy(certificateSeenByUser = true)).also {
            Timber.tag(TAG).d("markAsSeenByUser %s", it.containerId)
        }
    }

    private fun VaccinationCertificateQRCode.createContainer(
        nowUtc: Instant = timeStamper.nowUTC
    ): VaccinationCertificateContainer =
        toVaccinationContainer(
            scannedAt = nowUtc,
            qrCodeExtractor = qrCodeExtractor,
            certificateSeenByUser = false,
        )

    private fun VaccinationCertificateContainer.toVaccinationCertificateOrNull(
        valueSet: VaccinationValueSets?,
        certificateState: CwaCovidCertificate.State
    ): VaccinationCertificate? {
        try {
            return toVaccinationCertificate(valueSet, certificateState).also {
                // read value from dcc data to throw an exception early if the DccQrCodeExtractor is not able to parse
                // the certificate
                it.personIdentifier
            }
        } catch (e: Exception) {
            Timber.e(e, "Creating VaccinationCertificate failed")
        }
        return null
    }

    private suspend fun VaccinationCertificateContainer.toVaccinationCertificateWrapper(
        valueSets: VaccinationValueSets,
        dccValidityMeasures: DccValidityMeasures
    ): VaccinationCertificateWrapper? {
        try {
            val state = dccState(
                dccData = certificateData,
                qrCodeHash = qrCodeHash,
                dccValidityMeasures = dccValidityMeasures
            )

            return VaccinationCertificateWrapper(
                valueSets = valueSets,
                container = this,
                certificateState = state
            )
        } catch (e: Exception) {
            Timber.e(e, "Creating VaccinationCertificateWrapper failed")
        }
        return null
    }

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
