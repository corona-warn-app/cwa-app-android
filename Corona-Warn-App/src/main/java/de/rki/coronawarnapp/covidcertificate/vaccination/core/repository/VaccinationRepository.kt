package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
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
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val storage: VaccinationStorage,
    valueSetsRepository: ValueSetsRepository,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val dccStateChecker: DccStateChecker,
    dscRepository: DscRepository
) {

    private val internalData: HotDataFlow<Set<VaccinatedPerson>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.load()
            .map { personContainer ->
                VaccinatedPerson(
                    data = personContainer,
                    certificateStates = personContainer.getStates(),
                    valueSet = null,
                )
            }
            .toSet()
            .also { Timber.tag(TAG).v("Restored vaccination data: %s", it) }
    }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing VaccinationContainer data.") }
            .drop(1) // Initial emission, restored from storage.
            .onEach { vaccinatedPersons ->
                Timber.tag(TAG).v("Vaccination data changed: %s", vaccinatedPersons)
                storage.save(vaccinatedPersons.map { it.data }.toSet())
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot vaccination data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = combine(
        internalData.data,
        valueSetsRepository.latestVaccinationValueSets,
        dscRepository.dscData
    ) { personDatas, currentValueSet, _ ->
        personDatas.map { person ->
            val stateMap = person.data.getStates()
            person.copy(valueSet = currentValueSet, certificateStates = stateMap)
        }.toSet()
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

            if (matchingPerson.data.vaccinations.any { it.certificateId == qrCode.uniqueCertificateIdentifier }) {
                Timber.tag(TAG).e("Certificate is already registered: %s", qrCode.uniqueCertificateIdentifier)
                throw InvalidVaccinationCertificateException(ALREADY_REGISTERED)
            }

            val newCertificate = qrCode.toVaccinationContainer(
                scannedAt = timeStamper.nowUTC,
                qrCodeExtractor = qrCodeExtractor,
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
            it.certificateId == qrCode.uniqueCertificateIdentifier
        }
    }

    /**
     * Passing null as identifier will refresh all available data, if within constraints.
     * Throws VaccinatedPersonNotFoundException is you try to refresh a person that is unknown.
     */
    suspend fun refresh(personIdentifier: CertificatePersonIdentifier? = null) {
        Timber.tag(TAG).d("refresh(personIdentifier=%s)", personIdentifier)

        // NOOP
    }

    suspend fun clear() {
        Timber.tag(TAG).w("Clearing vaccination data.")
        internalData.updateBlocking {
            Timber.tag(TAG).v("Deleting: %s", this)
            emptySet()
        }
    }

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

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
