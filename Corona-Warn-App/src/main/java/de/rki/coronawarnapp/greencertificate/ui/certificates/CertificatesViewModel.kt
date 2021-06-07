package de.rki.coronawarnapp.greencertificate.ui.certificates

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.cards.BottomInfoVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.CreateVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.HeaderInfoVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.VaccinationCard
import kotlinx.coroutines.flow.map

class CertificatesViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    private val vaccinationSettings: VaccinationSettings
) : CWAViewModel() {

    val events = SingleLiveEvent<CertificatesFragmentEvents>()

    val screenItems: LiveData<List<CertificatesItem>> =
        vaccinationRepository.vaccinationInfos.map { vaccinatedPersons ->
            mutableListOf<CertificatesItem>().apply {
                add(HeaderInfoVaccinationCard.Item)
                addVaccinationCards(vaccinatedPersons)
                add(BottomInfoVaccinationCard.Item)
            }
        }.asLiveData()

    private fun MutableList<CertificatesItem>.addVaccinationCards(vaccinatedPersons: Set<VaccinatedPerson>) {
        vaccinatedPersons.forEach { vaccinatedPerson ->
            val card = when (vaccinatedPerson.getVaccinationStatus()) {
                VaccinatedPerson.Status.COMPLETE,
                VaccinatedPerson.Status.INCOMPLETE -> VaccinationCard.Item(
                    vaccinatedPerson = vaccinatedPerson,
                    onClickAction = {
                        events.postValue(
                            CertificatesFragmentEvents.GoToVaccinationList(
                                vaccinatedPerson.identifier.codeSHA256
                            )
                        )
                    }
                )
                VaccinatedPerson.Status.IMMUNITY -> ImmuneVaccinationCard.Item(
                    vaccinatedPerson = vaccinatedPerson,
                    onClickAction = {
                        events.postValue(
                            CertificatesFragmentEvents.GoToVaccinationList(
                                vaccinatedPerson.identifier.codeSHA256
                            )
                        )
                    }
                )
            }
            add(card)
        }
        if (vaccinatedPersons.isEmpty()) {
            add(
                CreateVaccinationCard.Item(
                    onClickAction = {
                        events.postValue(
                            CertificatesFragmentEvents.OpenVaccinationRegistrationGraph(
                                vaccinationSettings.registrationAcknowledged
                            )
                        )
                    }
                )
            )
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CertificatesViewModel>
}
