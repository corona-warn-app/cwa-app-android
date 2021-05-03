package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.mutate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.server.VaccinationProofServer
import de.rki.coronawarnapp.vaccination.core.server.VaccinationServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    private val storage: VaccinationStorage,
    private val vaccinationServer: VaccinationServer,
    private val vaccinationProofServer: VaccinationProofServer,
) {

    private val internalData: HotDataFlow<Map<VaccinatedPersonIdentifier, VaccinatedPerson>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        storage.vaccinatedPersons.map { it.identifier to it }.toMap().also {
            Timber.tag(TAG).v("Restored vaccination data: %s", it)
        }
    }

    init {
        internalData.data
            .onStart { Timber.tag(CoronaTestRepository.TAG).d("Observing test data.") }
            .onEach {
                Timber.tag(TAG).v("Vaccination data changed: %s", it)
                storage.vaccinatedPersons = it.values.toSet()
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot vaccination data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.IO)
    }

    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = internalData.data.map { it.values.toSet() }

    suspend fun registerVaccination(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        throw NotImplementedError()
    }

    suspend fun clear() {
        Timber.tag(TAG).w("Clearing vaccination data.")
        internalData.updateBlocking {
            Timber.tag(TAG).v("Deleting: %s", this)
            emptyMap()
        }
    }

    suspend fun deleteVaccinationCertificate(vaccinationCertificateId: String) {
        Timber.tag(TAG).w("deleteVaccinationCertificate(certificateId=%s)", vaccinationCertificateId)
        internalData.updateBlocking {
            val target = values.find { person ->
                person.vaccinationCertificates.any { it.certificateId == vaccinationCertificateId }
            } ?: throw VaccinationCertificateNotFoundException(
                "No vaccination certificate found for $vaccinationCertificateId"
            )

            this.mutate {
                this[target.identifier] = target.copy(
                    vaccinationCertificates = target.vaccinationCertificates.filter {
                        it.certificateId != vaccinationCertificateId
                    }.toSet()
                )
            }
        }
    }

    companion object {
        private const val TAG = "VaccinationRepository"
    }
}
