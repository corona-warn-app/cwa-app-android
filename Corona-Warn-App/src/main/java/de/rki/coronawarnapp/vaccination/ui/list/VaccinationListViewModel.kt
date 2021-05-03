package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.first

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val vaccinatedPersonIdentifier: String,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    private val _vaccinatedPerson: MutableLiveData<VaccinatedPerson> = MutableLiveData()
    val vaccinatedPerson = _vaccinatedPerson

    init {
        // TODO: load real values from repository
        launch {
            val person = when (vaccinatedPersonIdentifier) {
                "vaccinated-person-incomplete" -> vaccinationRepository.vaccinationInfos.first().first()
                "vaccinated-person-complete" -> vaccinationRepository.vaccinationInfos.first().elementAt(1)
                else -> throw IllegalArgumentException()
            }
            vaccinatedPerson.postValue(person)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
