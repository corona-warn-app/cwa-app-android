package de.rki.coronawarnapp.greencertificate.ui.certificates

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestCertificateRepository
import de.rki.coronawarnapp.coronatest.type.TestCertificateContainer
import de.rki.coronawarnapp.coronatest.type.TestCertificateIdentifier
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.cards.NoCovidTestCertificatesCard
import de.rki.coronawarnapp.vaccination.ui.cards.CreateVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.HeaderInfoVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.VaccinationCard
import de.rki.coronawarnapp.vaccination.ui.cards.CovidTestCertificateErrorCard
import de.rki.coronawarnapp.vaccination.ui.cards.CovidTestCertificateCard
import kotlinx.coroutines.flow.combine

class CertificatesViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    private val vaccinationSettings: VaccinationSettings,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel() {

    val events = SingleLiveEvent<CertificatesFragmentEvents>()

    private fun refreshTestCertificate(identifier: TestCertificateIdentifier) {
        launch {
            testCertificateRepository.refresh(identifier)
        }
    }

    val screenItems: LiveData<List<CertificatesItem>> =
        vaccinationRepository.vaccinationInfos
            .combine(testCertificateRepository.certificates) { vaccinatedPersons, certificates ->
                mutableListOf<CertificatesItem>().apply {
                    add(HeaderInfoVaccinationCard.Item)
                    addVaccinationCards(vaccinatedPersons)
                    addTestCertificateCards(certificates)
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

    private fun MutableList<CertificatesItem>.addTestCertificateCards(certificates: Set<TestCertificateContainer>) {

        certificates.forEach { certificate ->
            if (certificate.isCertificateRetrievalPending) {
                add(
                    CovidTestCertificateErrorCard.Item(
                        testDate = certificate.registeredAt,
                        onClickAction = {
                            refreshTestCertificate(certificate.identifier)
                        }
                    )
                )
            } else {
                add(
                    CovidTestCertificateCard.Item(
                        testDate = certificate.registeredAt,
                        testPerson =
                        certificate.toTestCertificate(null)?.firstName + " " +
                            certificate.toTestCertificate(null)?.lastName
                    )
                )
            }
        }

        if (certificates.isEmpty()) {
            add(NoCovidTestCertificatesCard.Item)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CertificatesViewModel>
}
