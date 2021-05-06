package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.personIdentifier
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.repository.errors.VaccinatedPersonNotFoundException
import de.rki.coronawarnapp.vaccination.core.repository.errors.VaccinationCertificateNotFoundException
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.repository.storage.toProofContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.toVaccinationContainer
import de.rki.coronawarnapp.vaccination.core.server.VaccinationProofServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val storage: VaccinationStorage,
    private val valueSetsRepository: ValueSetsRepository,
    private val vaccinationProofServer: VaccinationProofServer,
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
                    data = VaccinatedPersonData(
                        vaccinations = emptySet(),
                        proofs = emptySet()
                    ),
                    valueSet = null,
                )
            }

            val newCertificate = qrCode.toVaccinationContainer(scannedAt = timeStamper.nowUTC)

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

        if (updatedPerson.isEligbleForProofCertificate) {
            Timber.tag(TAG).i("%s is eligble for proof certificate, launching async check.", updatedPerson.identifier)
            appScope.launch {
                refresh(updatedPerson.identifier)
            }
        }

        return updatedPerson.vaccinationCertificates.single {
            it.certificateId == qrCode.uniqueCertificateIdentifier
        }
    }

    suspend fun checkForProof(personIdentifier: VaccinatedPersonIdentifier?) {
        Timber.tag(TAG).i("checkForProof(personIdentifier=%s)", personIdentifier)
        withContext(appScope.coroutineContext) {
            internalData.updateBlocking {
                val originalPerson = this.singleOrNull {
                    it.identifier == personIdentifier
                } ?: throw VaccinatedPersonNotFoundException("Identifier=$personIdentifier")

                val eligbleCert = originalPerson.data.vaccinations.first { it.isEligbleForProofCertificate }

                val proof = try {
                    vaccinationProofServer.getProofCertificate(eligbleCert.vaccinationCertificateCOSE)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to check for proof.")
                    null
                }

                val modifiedPerson = proof?.let {
                    originalPerson.copy(
                        data = originalPerson.data.copy(
                            proofs = setOf(it.toProofContainer(timeStamper.nowUTC))
                        )
                    )
                } ?: originalPerson

                this.toMutableSet().apply {
                    remove(originalPerson)
                    add(modifiedPerson)
                }
            }
        }
        throw NotImplementedError()
    }

    suspend fun refresh(personIdentifier: VaccinatedPersonIdentifier?) {
        Timber.tag(TAG).d("refresh(personIdentifier=%s)", personIdentifier)
        // TODO
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
        internalData.updateBlocking {
            val target = this.find { person ->
                person.vaccinationCertificates.any { it.certificateId == vaccinationCertificateId }
            } ?: throw VaccinationCertificateNotFoundException(
                "No vaccination certificate found for $vaccinationCertificateId"
            )

            val newTarget = target.copy(
                data = target.data.copy(
                    vaccinations = target.data.vaccinations.filter {
                        it.certificateId != vaccinationCertificateId
                    }.toSet()
                )
            )

            this.map {
                if (it != target) newTarget else it
            }.toSet()
        }
    }

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
