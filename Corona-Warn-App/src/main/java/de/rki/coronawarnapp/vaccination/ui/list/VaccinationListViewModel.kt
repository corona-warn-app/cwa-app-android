package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.first

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    val vaccinationListItems: MutableLiveData<List<VaccinationListItem>> = MutableLiveData()

    init {
        launch {
            val person = when (vaccinatedPersonIdentifier) {
                "vaccinated-person-incomplete" -> vaccinationRepository.vaccinationInfos.first().first()
                "vaccinated-person-complete" -> vaccinationRepository.vaccinationInfos.first().elementAt(1)
                else -> throw IllegalArgumentException()
            }

            vaccinationListItems.postValue(
                mutableListOf<VaccinationListItem>().apply {
                    add(VaccinationListIncompleteTopCardItem)
                    add(
                        VaccinationListNameCardItem(
                            fullName = "Andrea Schneider",
                            dayOfBirth = person.dateOfBirth.toDayFormat()
                        )
                    )
                    person.vaccinationCertificates.forEach { vaccinationCertificate ->
                        add(
                            VaccinationListVaccinationCardItem(
                                vaccinationCertificateId = vaccinationCertificate.certificateId,
                                // Todo: use values from repository
                                doseNumber = "1",
                                totalSeriesOfDoses = "2",
                                vaccinatedAt = vaccinationCertificate.vaccinatedAt.toDayFormat(),
                                vaccinationStatus = person.vaccinationStatus,
                                isFinalVaccination = false
                            )
                        )
                    }
                }.toList()
            )
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
