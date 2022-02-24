package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.toVaccinationContainer
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.shareLatest
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
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    valueSetsRepository: ValueSetsRepository,
    private val timeStamper: TimeStamper,
    private val storage: VaccinationStorage,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val dccStateChecker: DccStateChecker,
    @AppScope private val appScope: CoroutineScope,
    dscRepository: DscRepository
) {

    private val internalData: HotDataFlow<Set<VaccinationContainer>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.load2()
            .map {
                VaccinationContainer(
                    data = it,
                    qrCodeExtractor = qrCodeExtractor
                )
            }
            .toSet()
            .also { Timber.tag(TAG).v("Restored vaccination data, %d items", it.size) }
    }

    val freshCertificates: Flow<Set<VaccinationCertificateWrapper>> = de.rki.coronawarnapp.util.flow.combine(
        internalData.data,
        valueSetsRepository.latestVaccinationValueSets,
        dscRepository.dscData
    ) { set, valueSets, _ ->
        set
            .filter { it.isNotRecycled }
            .map { container ->
                val state = dccStateChecker.checkState(container.certificateData).first()
                VaccinationCertificateWrapper(
                    valueSets = valueSets,
                    container = container,
                    certificateState = state
                )
            }.toSet()
    }

    val certificates: Flow<Set<VaccinationCertificateWrapper>> = freshCertificates
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing VaccinationContainer data.") }
            .drop(1) // Initial emission, restored from storage.
            .onEach { certificates ->
                Timber.tag(TAG).v("Vaccination data changed, %d items", certificates.size)
                storage.save2(certificates.map { it.data }.toSet())
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot vaccination data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    /**
     * Returns a flow with a set of [VaccinationCertificate] matching the predicate [VaccinationCertificate.isRecycled]
     */
    val recycledCertificates: Flow<Set<VaccinationCertificate>> = internalData.data
        .map { container ->
            container
                .filter { it.isRecycled }
                .map {
                    it.toVaccinationCertificate(certificateState = CwaCovidCertificate.State.Recycled)
                }
                .toSet()
        }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    suspend fun registerCertificate(
        qrCode: VaccinationCertificateQRCode,
    ): VaccinationCertificate {
        Timber.tag(TAG).v("registerVaccination(qrCode=%s)", qrCode)

        val updatedData = internalData.updateBlocking {
            val matchingPerson = this.singleOrNull {
                it.identifier == qrCode.personIdentifier
            } ?: VaccinatedPerson(
                data = VaccinatedPersonData(),
                certificateStates = emptyMap(),
                valueSet = null,
            ).also { Timber.tag(TAG).i("Creating new person for %s", qrCode) }

            if (matchingPerson.data.vaccinations.any { it.qrCodeHash == qrCode.hash }) {
                Timber.tag(TAG).e("Certificate is already registered: %s", qrCode.hash)
                throw InvalidVaccinationCertificateException(ALREADY_REGISTERED)
            }

            val newCertificate = qrCode.toVaccinationContainer(
                scannedAt = timeStamper.nowUTC,
                qrCodeExtractor = qrCodeExtractor,
                certificateSeenByUser = false,
            )

            val newPersonData = matchingPerson.data.copy(
                vaccinations = matchingPerson.data.vaccinations.plus(newCertificate)
            )

            val modifiedPerson = matchingPerson.copy(
                data = newPersonData,
                certificateStates = newPersonData.getStates()
            )

            this.toMutableSet().apply {
                remove(matchingPerson)
                add(modifiedPerson)
            }
        }

        val updatedPerson = updatedData.single { it.identifier == qrCode.personIdentifier }

        return updatedPerson.vaccinationCertificates.single {
            it.qrCodeHash == qrCode.hash
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).w("Clearing vaccination data.")
        internalData.updateBlocking {
            Timber.tag(TAG).v("Deleting %d items", this.size)
            emptySet()
        }
    }

    // TODO: remove
    suspend fun deleteCertificate(containerId: VaccinationCertificateContainerId): VaccinationContainer? {
        Timber.tag(TAG).w("deleteCertificate(containerId=%s)", containerId)
        var deletedVaccination: VaccinationContainer? = null

        internalData.updateBlocking {
            val target = this.find { person ->
                person.vaccinationContainers.any { it.containerId == containerId }
            }

            if (target == null) {
                Timber.tag(TAG).w("Can't find certificate, doesn't exist? (%s)", containerId)
                return@updateBlocking this
            }

            deletedVaccination = target.vaccinationContainers.single {
                it.containerId == containerId
            }

            val newTarget = if (target.data.vaccinations.size > 1) {
                target.copy(
                    data = target.data.copy(
                        vaccinations = target.data.vaccinations.filter { it != deletedVaccination }.toSet()
                    )
                )
            } else {
                Timber.tag(TAG).w("Person has no certificate after removal, removing person.")
                null
            }

            this.mapNotNull { if (it == target) newTarget else it }.toSet()
        }

        return deletedVaccination?.also {
            Timber.tag(TAG).i("Deleted: %s", containerId)
        }
    }

    private suspend fun VaccinatedPersonData.getStates(): Map<
        VaccinationCertificateContainerId,
        CwaCovidCertificate.State
        > {
        return vaccinations.associate { container ->
            val state = dccStateChecker.checkState(container.certificateData).first()
            container.containerId to state
        }
    }

    suspend fun setNotifiedState(
        containerId: VaccinationCertificateContainerId,
        state: CwaCovidCertificate.State,
        time: Instant?,
    ) {
        Timber.tag(TAG).d("setNotifiedAboutState(containerId=$containerId, time=$time)")
        internalData.updateBlocking {
            val toUpdatePerson = singleOrNull { it.findVaccination(containerId) != null }

            if (toUpdatePerson == null) {
                Timber.tag(TAG).w("Couldn't find %s", containerId)
                return@updateBlocking this
            }

            val toUpdateVaccination = toUpdatePerson.findVaccination(containerId)!!

            val newVaccination = when (state) {
                is CwaCovidCertificate.State.Expired -> toUpdateVaccination.copy(notifiedExpiredAt = time)
                is CwaCovidCertificate.State.ExpiringSoon -> toUpdateVaccination.copy(notifiedExpiresSoonAt = time)
                is CwaCovidCertificate.State.Invalid -> toUpdateVaccination.copy(notifiedInvalidAt = time)
                is CwaCovidCertificate.State.Blocked -> toUpdateVaccination.copy(notifiedBlockedAt = time)
                else -> throw UnsupportedOperationException("$state is not supported.")
            }

            newVaccination.qrCodeExtractor = qrCodeExtractor

            val newPerson = toUpdatePerson.copy(
                data = toUpdatePerson.data.copy(
                    vaccinations = toUpdatePerson.data.vaccinations.minus(toUpdateVaccination).plus(newVaccination)
                )
            )

            this.minus(toUpdatePerson).plus(newPerson)
        }
    }

    suspend fun acknowledgeState(containerId: VaccinationCertificateContainerId) {
        Timber.tag(TAG).d("acknowledgeStateChange(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdatePerson = singleOrNull { it.findVaccination(containerId) != null }

            if (toUpdatePerson == null) {
                Timber.tag(TAG).w("Couldn't find %s", containerId)
                return@updateBlocking this
            }

            val toUpdateVaccination = toUpdatePerson.findVaccination(containerId)!!
            val currentState = dccStateChecker.checkState(toUpdateVaccination.certificateData).first()

            Timber.tag(TAG)
                .d("Acknowledging state change to %s -> %s.", toUpdateVaccination.lastSeenStateChange, currentState)

            val newVaccination = toUpdateVaccination.copy(
                lastSeenStateChange = currentState,
                lastSeenStateChangeAt = timeStamper.nowUTC,
            )

            newVaccination.qrCodeExtractor = qrCodeExtractor

            val newPerson = toUpdatePerson.copy(
                data = toUpdatePerson.data.copy(
                    vaccinations = toUpdatePerson.data.vaccinations.minus(toUpdateVaccination).plus(newVaccination)
                )
            )

            this.minus(toUpdatePerson).plus(newPerson)
        }
    }

    suspend fun markAsSeenByUser(containerId: VaccinationCertificateContainerId) {
        Timber.tag(TAG).d("markAsSeenByUser(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdatePerson = singleOrNull { it.findVaccination(containerId) != null }

            if (toUpdatePerson == null) {
                Timber.tag(TAG).w("markAsSeenByUser Couldn't find %s", containerId)
                return@updateBlocking this
            }

            val toUpdateVaccination = toUpdatePerson.findVaccination(containerId)!!
            val newVaccination = toUpdateVaccination.copy(certificateSeenByUser = true)
            newVaccination.qrCodeExtractor = qrCodeExtractor
            val newPerson = toUpdatePerson.copy(
                data = toUpdatePerson.data.copy(
                    vaccinations = toUpdatePerson.data.vaccinations.minus(toUpdateVaccination).plus(newVaccination)
                )
            )
            this.minus(toUpdatePerson).plus(newPerson)
        }
    }

    suspend fun acknowledgeBoosterRule(personIdentifierCode: String, boosterIdentifier: String) {
        Timber.tag(TAG).d("acknowledgeBoosterRule(personIdentifierCode=%s)", personIdentifierCode)
        internalData.updateBlocking {
            val vaccinatedPerson = singleOrNull { it.identifier.codeSHA256 == personIdentifierCode }

            if (vaccinatedPerson == null) {
                Timber.tag(TAG).w("acknowledgeBoosterRule couldn't find person %s", personIdentifierCode)
                return@updateBlocking this
            }

            val updatedPerson = vaccinatedPerson.copy(
                data = vaccinatedPerson.data.copy(
                    lastSeenBoosterRuleIdentifier = boosterIdentifier
                )
            )

            Timber.tag(TAG).d("acknowledgeBoosterRule updatedPerson=%s", updatedPerson.identifier)

            this.minus(vaccinatedPerson).plus(updatedPerson)
        }
    }

    suspend fun clearBoosterRuleInfo(
        personIdentifier: CertificatePersonIdentifier
    ) {
        Timber.tag(TAG)
            .d("clearBoosterRuleInfo(personIdentifier=%s)", personIdentifier)

        internalData.updateBlocking {

            val vaccinatedPerson = singleOrNull { it.identifier == personIdentifier }

            if (vaccinatedPerson == null) {
                Timber.tag(TAG).w("clearBoosterRuleInfo couldn't find person %s", personIdentifier.codeSHA256)
                return@updateBlocking this
            }

            val data =
                vaccinatedPerson.data.copy(boosterRuleIdentifier = null, lastSeenBoosterRuleIdentifier = null)
            val updatedPerson = vaccinatedPerson.copy(data = data)

            Timber.tag(TAG).d("clearBoosterRuleInfo updatedPerson=%s", data.identifier)

            this.minus(vaccinatedPerson).plus(updatedPerson)
        }
    }

    suspend fun updateBoosterNotifiedAt(
        personIdentifier: CertificatePersonIdentifier,
        time: Instant
    ) {
        Timber.tag(TAG).d("updateBoosterNotifiedAt(personIdentifier=%s, time=%s)", personIdentifier.codeSHA256, time)
        internalData.updateBlocking {
            val vaccinatedPerson = singleOrNull { it.identifier == personIdentifier }

            if (vaccinatedPerson == null) {
                Timber.tag(TAG).w("updateBoosterNotifiedAt couldn't find person %s", personIdentifier.codeSHA256)
                return@updateBlocking this
            }

            val updatedPerson = vaccinatedPerson.copy(
                data = vaccinatedPerson.data.copy(lastBoosterNotifiedAt = time)
            )

            Timber.tag(TAG).d("updateBoosterNotifiedAt updatedPerson=%s", updatedPerson.identifier)

            this.minus(vaccinatedPerson).plus(updatedPerson)
        }
    }

    /**
     * Move Vaccination certificate to recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun recycleCertificate(containerId: VaccinationCertificateContainerId) {
        Timber.tag(TAG).d("recycleCertificate(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdatePerson = singleOrNull { it.findVaccination(containerId) != null }

            if (toUpdatePerson == null) {
                Timber.tag(TAG).w("recycleCertificate couldn't find %s", containerId)
                return@updateBlocking this
            }

            val toUpdateVaccination = toUpdatePerson.findVaccination(containerId)!!
            val newVaccination = toUpdateVaccination.copy(recycledAt = timeStamper.nowUTC)
            newVaccination.qrCodeExtractor = qrCodeExtractor
            val newPerson = toUpdatePerson.copy(
                data = toUpdatePerson.data.copy(
                    vaccinations = toUpdatePerson.data.vaccinations.minus(toUpdateVaccination).plus(newVaccination)
                )
            )
            this.minus(toUpdatePerson).plus(newPerson)
        }
    }

    /**
     * Restore Vaccination certificate from recycled state.
     * it does not throw any exception if certificate is not found
     */
    suspend fun restoreCertificate(containerId: VaccinationCertificateContainerId) {
        Timber.tag(TAG).d("restoreCertificate(containerId=$containerId)")
        internalData.updateBlocking {
            val toUpdatePerson = singleOrNull { it.findVaccination(containerId) != null }

            if (toUpdatePerson == null) {
                Timber.tag(TAG).w("restoreCertificate couldn't find %s", containerId)
                return@updateBlocking this
            }

            val toUpdateVaccination = toUpdatePerson.findVaccination(containerId)!!
            val newVaccination = toUpdateVaccination.copy(recycledAt = null)
            newVaccination.qrCodeExtractor = qrCodeExtractor
            val newPerson = toUpdatePerson.copy(
                data = toUpdatePerson.data.copy(
                    vaccinations = toUpdatePerson.data.vaccinations.minus(toUpdateVaccination).plus(newVaccination)
                )
            )
            this.minus(toUpdatePerson).plus(newPerson)
        }
    }

    suspend fun replaceCertificate(
        certificateToReplace: VaccinationCertificateContainerId,
        newCertificateQrCodeQrCode: VaccinationCertificateQRCode
    ) {
        // TO_DO("https://jira-ibs.wbs.net.sap/browse/EXPOSUREAPP-11940")
    }

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
