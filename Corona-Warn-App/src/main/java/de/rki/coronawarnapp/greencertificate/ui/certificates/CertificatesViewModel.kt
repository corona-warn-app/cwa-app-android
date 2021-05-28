package de.rki.coronawarnapp.greencertificate.ui.certificates

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.homecard.CreateVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecard.ImmuneVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecard.VaccinationHomeCard
import kotlinx.coroutines.flow.map

class CertificatesViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    private val vaccinationSettings: VaccinationSettings
) : CWAViewModel() {

    val events = SingleLiveEvent<HomeFragmentEvents>()

    val screenItems: LiveData<List<CertificatesItem>> =
        vaccinationRepository.vaccinationInfos.map { vaccinatedPersons ->
            mutableListOf<CertificatesItem>().apply {
                vaccinatedPersons.forEach { vaccinatedPerson ->
                    val card = when (vaccinatedPerson.getVaccinationStatus()) {
                        VaccinatedPerson.Status.COMPLETE,
                        VaccinatedPerson.Status.INCOMPLETE -> VaccinationHomeCard.Item(
                            vaccinatedPerson = vaccinatedPerson,
                            onClickAction = {
                                events.postValue(
                                    HomeFragmentEvents.GoToVaccinationList(vaccinatedPerson.identifier.codeSHA256)
                                )
                            }
                        )
                        VaccinatedPerson.Status.IMMUNITY -> ImmuneVaccinationHomeCard.Item(
                            vaccinatedPerson = vaccinatedPerson,
                            onClickAction = {
                                events.postValue(
                                    HomeFragmentEvents.GoToVaccinationList(vaccinatedPerson.identifier.codeSHA256)
                                )
                            }
                        )
                    }
                    add(card)
                }

                add(
                    CreateVaccinationHomeCard.Item(
                        onClickAction = {
                            events.postValue(
                                HomeFragmentEvents.OpenVaccinationRegistrationGraph(
                                    vaccinationSettings.registrationAcknowledged
                                )
                            )
                        }
                    )
                )
            }
        }.asLiveData()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CertificatesViewModel>
}
