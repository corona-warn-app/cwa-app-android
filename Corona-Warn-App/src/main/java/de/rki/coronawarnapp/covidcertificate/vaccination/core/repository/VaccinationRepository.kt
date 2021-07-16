package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
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
) {

    private val internalData: HotDataFlow<Set<VaccinatedPerson>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.load()
            .map { personContainer ->
                VaccinatedPerson(
                    data = personContainer,
                    valueSet = null,
                    isUpdatingData = false,
                    lastError = null
                )
            }
            .toSet()
            .also { Timber.tag(TAG).v("Restored vaccination data: %s", it) }
    }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing VaccinationContainer data.") }
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
        valueSetsRepository.latestVaccinationValueSets
    ) { personDatas, currentValueSet ->
        personDatas.map { it.copy(valueSet = currentValueSet) }.toSet()
    }

    suspend fun registerCertificate(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        Timber.tag(TAG).v("registerVaccination(qrCode=%s)", qrCode)

        val updatedData = internalData.updateBlocking {
            val matchingPerson = this.singleOrNull {
                it.identifier == qrCode.personIdentifier
            } ?: VaccinatedPerson(
                data = VaccinatedPersonData(),
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

            val modifiedPerson = matchingPerson.copy(
                data = matchingPerson.data.copy(
                    vaccinations = matchingPerson.data.vaccinations.plus(newCertificate)
                )
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

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
