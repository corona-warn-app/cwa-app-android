package de.rki.coronawarnapp.vaccination.ui.details

import androidx.lifecycle.liveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository

class VaccinationDetailsViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val certificateId: String,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    val vaccinationCertificate = liveData(context = dispatcherProvider.Default) {
        emit(vaccinationRepository.vaccinationCertificateFor(certificateId))
    }

    fun deleteVaccination() = launch {
        vaccinationRepository.deleteVaccinationCertificate("certificateId")
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationDetailsViewModel> {
        fun create(
            certificateId: String,
        ): VaccinationDetailsViewModel
    }
}
