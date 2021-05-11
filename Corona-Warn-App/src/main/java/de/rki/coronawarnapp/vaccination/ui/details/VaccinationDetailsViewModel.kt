package de.rki.coronawarnapp.vaccination.ui.details

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.map
import timber.log.Timber

class VaccinationDetailsViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val vaccinationCertificateId: String,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    val vaccinationCertificate = vaccinationRepository.vaccinationInfos.map {
        findVaccinationDetails(it)
    }.asLiveData(context = dispatcherProvider.Default)

    val errors = SingleLiveEvent<Throwable>()
    val events = SingleLiveEvent<VaccinationDetailsNavigation>()

    fun deleteVaccination() = launch {
        try {
            Timber.d("deleteVaccination")
            vaccinationRepository.deleteVaccinationCertificate(vaccinationCertificateId)
            events.postValue(VaccinationDetailsNavigation.Back)
        } catch (e: Exception) {
            Timber.d(e, "deleteVaccinationCertificate failed")
            errors.postValue(e)
        }
    }

    fun onClose() {
        events.postValue(VaccinationDetailsNavigation.Back)
    }

    private fun findVaccinationDetails(vaccinatedPersons: Set<VaccinatedPerson>): VaccinationDetails {
        val vaccinatedPerson = vaccinatedPersons.find { vaccinatedPerson ->
            vaccinatedPerson
                .vaccinationCertificates
                .any { it.certificateId == vaccinationCertificateId }
        }

        return VaccinationDetails(
            certificate = vaccinatedPerson?.vaccinationCertificates?.find {
                it.certificateId == vaccinationCertificateId
            },
            isComplete = vaccinatedPerson?.vaccinationStatus == VaccinatedPerson.Status.COMPLETE
        )
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationDetailsViewModel> {
        fun create(
            certificateId: String,
        ): VaccinationDetailsViewModel
    }
}

data class VaccinationDetails(
    val certificate: VaccinationCertificate?,
    val isComplete: Boolean = false
)
