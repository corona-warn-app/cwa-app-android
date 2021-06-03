package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.vaccination.core.CertificatePersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.vaccination.core.personIdentifier
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeExtractor
import de.rki.coronawarnapp.vaccination.core.repository.errors.VaccinationCertificateNotFoundException
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.repository.storage.toVaccinationContainer
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
    private val vaccinationQRCodeExtractor: VaccinationQRCodeExtractor,
) {

    private val internalData: HotDataFlow<Set<VaccinatedPerson>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.personContainers
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
            .onStart { Timber.tag(TAG).d("Observing test data.") }
            .onEach { vaccinatedPersons ->
                Timber.tag(TAG).v("Vaccination data changed: %s", vaccinatedPersons)
                storage.personContainers = vaccinatedPersons.map { it.data }.toSet()
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot vaccination data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = combine(
        internalData.data,
        valueSetsRepository.latestValueSet
    ) { personDatas, currentValueSet ->
        personDatas.map { it.copy(valueSet = currentValueSet) }.toSet()
    }

    suspend fun registerVaccination(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        Timber.tag(TAG).v("registerVaccination(qrCode=%s)", qrCode)

        val updatedData = internalData.updateBlocking {
            val originalPerson = if (this.isNotEmpty()) {
                Timber.tag(TAG).d("There is an existing person we must match.")
                this.single().also {
                    it.identifier.requireMatch(qrCode.personIdentifier)
                    Timber.tag(TAG).i("New certificate matches existing person!")
                }
            } else {
                VaccinatedPerson(
                    data = VaccinatedPersonData(),
                    valueSet = null,
                )
            }

            if (originalPerson.data.vaccinations.any { it.certificateId == qrCode.uniqueCertificateIdentifier }) {
                Timber.tag(TAG).e("Certificate is already registered: %s", qrCode.uniqueCertificateIdentifier)
                throw InvalidHealthCertificateException(ErrorCode.VC_ALREADY_REGISTERED)
            }

            val newCertificate = qrCode.toVaccinationContainer(
                scannedAt = timeStamper.nowUTC,
                qrCodeExtractor = vaccinationQRCodeExtractor,
            )

            val modifiedPerson = originalPerson.copy(
                data = originalPerson.data.copy(
                    vaccinations = originalPerson.data.vaccinations.plus(newCertificate)
                )
            )

            this.toMutableSet().apply {
                remove(originalPerson)
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

    suspend fun deleteVaccinationCertificate(vaccinationCertificateId: String) {
        Timber.tag(TAG).w("deleteVaccinationCertificate(certificateId=%s)", vaccinationCertificateId)
        var deletedVaccination: VaccinationContainer? = null

        internalData.updateBlocking {
            val target = this.find { person ->
                person.vaccinationCertificates.any { it.certificateId == vaccinationCertificateId }
            } ?: throw VaccinationCertificateNotFoundException(
                "No vaccination certificate found for $vaccinationCertificateId"
            )

            deletedVaccination = target.data.vaccinations.single {
                it.certificateId == vaccinationCertificateId
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

        deletedVaccination?.let {
            Timber.tag(TAG).i("Deleted vaccination certificate: %s", it.certificateId)
        }
    }

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
