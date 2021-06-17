package de.rki.coronawarnapp.covidcertificate.test.ui

import android.content.Context
import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.CreateVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.HeaderInfoVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.NoCovidTestCertificatesCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.VaccinationCard
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class CertificatesViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    valueSetsRepository: ValueSetsRepository,
    @AppContext context: Context,
    private val vaccinationSettings: VaccinationSettings,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel() {

    init {
        valueSetsRepository.triggerUpdateValueSet(languageCode = context.getLocale())
    }

    val events = SingleLiveEvent<CertificatesFragmentEvents>()

    val screenItems: LiveData<List<CertificatesItem>> =
        vaccinationRepository.vaccinationInfos
            .combine(testCertificateRepository.certificates) { vaccinatedPersons, certificates ->
                mutableListOf<CertificatesItem>().apply {
                    add(HeaderInfoVaccinationCard.Item)
                    if (vaccinatedPersons.isEmpty()) {
                        add(
                            CreateVaccinationCard.Item(
                                onClickAction = {
                                    CertificatesFragmentEvents.OpenVaccinationRegistrationGraph(
                                        vaccinationSettings.registrationAcknowledged
                                    ).run { events.postValue(this) }
                                }
                            )
                        )
                    } else {
                        addAll(vaccinatedPersons.toCertificateItems())
                    }

                    if (certificates.isEmpty()) {
                        add(NoCovidTestCertificatesCard.Item)
                    }
                }
            }
            .asLiveData2()

    private fun Set<VaccinatedPerson>.toCertificateItems(): List<CertificatesItem> = map { vaccinatedPerson ->
        when (vaccinatedPerson.getVaccinationStatus()) {
            VaccinatedPerson.Status.COMPLETE,
            VaccinatedPerson.Status.INCOMPLETE -> VaccinationCard.Item(
                vaccinatedPerson = vaccinatedPerson,
                onClickAction = {
                    CertificatesFragmentEvents.GoToVaccinationList(
                        vaccinatedPerson.identifier.codeSHA256
                    ).run { events.postValue(this) }
                }
            )
            VaccinatedPerson.Status.IMMUNITY -> ImmuneVaccinationCard.Item(
                vaccinatedPerson = vaccinatedPerson,
                onClickAction = {
                    CertificatesFragmentEvents.GoToVaccinationList(
                        vaccinatedPerson.identifier.codeSHA256
                    ).run { events.postValue(this) }
                }
            )
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CertificatesViewModel>
}
