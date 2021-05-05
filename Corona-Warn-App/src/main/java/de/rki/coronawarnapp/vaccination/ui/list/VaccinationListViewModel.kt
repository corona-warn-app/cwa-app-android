package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.first

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    init {
        launch {
            // TODO: load real values from Repository
            val person = when (vaccinatedPersonIdentifier) {
                "vaccinated-person-incomplete" -> vaccinationRepository.vaccinationInfosForList.first().first()
                "vaccinated-person-complete" -> vaccinationRepository.vaccinationInfosForList.first().elementAt(1)
                else -> throw IllegalArgumentException()
            }

            val listItems = mutableListOf<VaccinationListItem>().apply {
                add(VaccinationListIncompleteTopCardItem)
                add(
                    VaccinationListNameCardItem(
                        fullName = "Andrea Schneider",
                        dayOfBirth = person.dateOfBirth.toDayFormat()
                    )
                )
                person.vaccinationCertificates.forEachIndexed { index, vaccinationCertificate ->
                    add(
                        VaccinationListVaccinationCardItem(
                            vaccinationCertificateId = vaccinationCertificate.certificateId,
                            // Todo: use properties from repository
                            doseNumber = (index + 1).toString(),
                            totalSeriesOfDoses = "2",
                            vaccinatedAt = vaccinationCertificate.vaccinatedAt.toDayFormat(),
                            vaccinationStatus = person.vaccinationStatus,
                            isFinalVaccination = (index + 1) == 2
                        )
                    )
                }
            }.toList()

            _uiState.postValue(UiState(listItems, vaccinationStatus = person.vaccinationStatus))
        }
    }

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
