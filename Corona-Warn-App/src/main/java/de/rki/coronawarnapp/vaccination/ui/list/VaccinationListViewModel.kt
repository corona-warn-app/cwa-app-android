package de.rki.coronawarnapp.vaccination.ui.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.core.repository.ValueSetsRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListIncompleteTopCardItemVH.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class VaccinationListViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    valueSetsRepository: ValueSetsRepository,
    @AppContext context: Context,
    @Assisted private val personIdentifierCodeSha256: String
) : CWAViewModel() {

    init {
        valueSetsRepository.reloadValueSet(languageCode = context.getLocale())
    }

    val events = SingleLiveEvent<Event>()

    private val vaccinatedPersonFlow = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->
        vaccinatedPersonSet.single { it.identifier.codeSHA256 == personIdentifierCodeSha256 }
    }

    val uiState: LiveData<UiState> = vaccinatedPersonFlow.map { vaccinatedPerson ->
        UiState(
            listItems = assembleItemList(vaccinatedPerson = vaccinatedPerson),
            vaccinationStatus = vaccinatedPerson.getVaccinationStatus()
        )
    }.catch {
        // TODO Error Handling in an upcoming subtask
    }.asLiveData()

    private fun assembleItemList(vaccinatedPerson: VaccinatedPerson) = mutableListOf<VaccinationListItem>().apply {
        if (vaccinatedPerson.getVaccinationStatus() == COMPLETE) {
            // Tbd what to show on complete vaccination - the proof certificate is now obsolete
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
                        vaccinationStatus = vaccinatedPerson.getVaccinationStatus(),
                        isFinalVaccination = doseNumber == totalSeriesOfDoses,
                        onCardClick = { certificateId ->
                            events.postValue(Event.NavigateToVaccinationCertificateDetails(certificateId))
                        },
                        onDeleteClick = { certificateId ->
                            events.postValue(Event.DeleteVaccinationEvent(certificateId))
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
        data class DeleteVaccinationEvent(val vaccinationCertificateId: String) : Event()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            personIdentifierCode: String
        ): VaccinationListViewModel
    }
}
