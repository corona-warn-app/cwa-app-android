package de.rki.coronawarnapp.vaccination.ui.details

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.map

class VaccinationDetailsViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val certificateId: String,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    val vaccinationCertificate = vaccinationRepository.vaccinationInfos.map {
        findVaccinationDetails(it)
    }.asLiveData(context = dispatcherProvider.Default)

    fun deleteVaccination() = launch {
        vaccinationRepository.deleteVaccinationCertificate(certificateId)
    }

    private fun findVaccinationDetails(vaccinatedPersons: Set<VaccinatedPerson>): VaccinationDetails {
        val vaccinatedPerson = vaccinatedPersons.find { vaccinatedPerson ->
            vaccinatedPerson
                .vaccinationCertificates
                .any { it.certificateId == certificateId }
        }

        return VaccinationDetails(
            certificate = vaccinatedPerson?.vaccinationCertificates?.find { it.certificateId == certificateId },
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
