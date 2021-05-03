package de.rki.coronawarnapp.vaccination.core.repository

import android.content.Context
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.server.VaccinationProofServer
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
    @AppContext private val context: Context,
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
                    person = personContainer,
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
                storage.personContainers = vaccinatedPersons.map { it.person }.toSet()
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
        throw NotImplementedError()
    }

    suspend fun refresh(personIdentifier: VaccinatedPersonIdentifier?) {
        Timber.tag(TAG).d("refresh(personIdentifier=%s)", personIdentifier)
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
                person = target.person.copy(
                    vaccinations = target.person.vaccinations.filter {
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
