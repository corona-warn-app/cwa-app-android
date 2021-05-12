package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListIncompleteTopCardItemVH.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class VaccinationListViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    val events = SingleLiveEvent<Event>()

    val vaccinationInfoFlow = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->
        // TODO
        // val vaccinatedPerson = vaccinatedPersonSet.single { it.identifier.code == vaccinatedPersonIdentifier }
        vaccinatedPersonSet.first()
    }

    val uiState: LiveData<UiState> = vaccinationInfoFlow.map { vaccinatedPerson ->

        val listItems = assembleItemList(
            vaccinatedPerson = vaccinatedPerson,
        )

        UiState(
            listItems = listItems,
            vaccinationStatus = vaccinatedPerson.vaccinationStatus
        )
    }.catch {
        // TODO Error Handling in an upcoming subtask
    }.asLiveData()

    private fun assembleItemList(vaccinatedPerson: VaccinatedPerson) = mutableListOf<VaccinationListItem>().apply {
        if (vaccinatedPerson.vaccinationStatus == COMPLETE) {
            // Tbd what to show on complete vaccination - the proof certificate is obsolete
        } else {
            add(VaccinationListIncompleteTopCardItem)
        }
        add(
            VaccinationListNameCardItem(
                fullName = "${vaccinatedPerson.firstName} ${vaccinatedPerson.lastName}",
                dayOfBirth = vaccinatedPerson.dateOfBirth.toDayFormat()
            )
        )
        vaccinatedPerson.vaccinationCertificates.forEach { vaccinationCertificate ->
            with(vaccinationCertificate) {
                add(
                    VaccinationListVaccinationCardItem(
                        vaccinationCertificateId = certificateId,
                        doseNumber = doseNumber.toString(),
                        totalSeriesOfDoses = totalSeriesOfDoses.toString(),
                        vaccinatedAt = vaccinatedAt.toDayFormat(),
                        vaccinationStatus = vaccinatedPerson.vaccinationStatus,
                        isFinalVaccination = doseNumber == totalSeriesOfDoses,
                        onCardClick = { certificateId ->
                            events.postValue(Event.NavigateToVaccinationCertificateDetails(certificateId))
                        }
                    )
                )
            }
        }
    }.toList()

    fun onRegisterNewVaccinationClick() {
        events.postValue(Event.NavigateToVaccinationQrCodeScanScreen)
    }

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status
    )

    sealed class Event {
        data class NavigateToVaccinationCertificateDetails(val vaccinationCertificateId: String) : Event()
        object NavigateToVaccinationQrCodeScanScreen : Event()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
