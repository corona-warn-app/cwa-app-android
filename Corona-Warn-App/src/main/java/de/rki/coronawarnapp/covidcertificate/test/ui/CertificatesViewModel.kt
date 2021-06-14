package de.rki.coronawarnapp.covidcertificate.test.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.test.ui.cards.CovidTestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.CreateVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.HeaderInfoVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.NoCovidTestCertificatesCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.VaccinationCard
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine

class CertificatesViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    private val vaccinationSettings: VaccinationSettings,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel() {

    val events = SingleLiveEvent<CertificatesFragmentEvents>()

    private fun refreshTestCertificate(identifier: TestCertificateIdentifier) {
        launch {
            val error = testCertificateRepository.refresh(identifier).mapNotNull { it.error }.singleOrNull()
            if (error != null) {
                events.postValue(CertificatesFragmentEvents.ShowRefreshErrorCertificateDialog(error))
            }
        }
    }

    fun deleteTestCertificate(identifier: TestCertificateIdentifier) {
        launch {
            testCertificateRepository.deleteCertificate(identifier)
        }
    }

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
                    } else {
                        addAll(certificates.toCertificateItems())
                    }
                }
            }.asLiveData()

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

    private fun Collection<TestCertificateWrapper>.toCertificateItems(): List<CertificatesItem> = map { certificate ->
        if (certificate.isCertificateRetrievalPending) {
            CovidTestCertificatePendingCard.Item(
                testDate = certificate.registeredAt,
                isUpdatingData = certificate.isUpdatingData,
                onRetryAction = {
                    refreshTestCertificate(certificate.identifier)
                },
                onDeleteAction = {
                    events.postValue(
                        CertificatesFragmentEvents.ShowDeleteErrorCertificateDialog(
                            certificate.identifier
                        )
                    )
                }
            )
        } else {
            CovidTestCertificateCard.Item(
                testDate = certificate.registeredAt,
                testPerson =
                certificate.testCertificate?.firstName + " " +
                    certificate.testCertificate?.lastName,
                onClickAction = {
                    CertificatesFragmentEvents.GoToCovidCertificateDetailScreen(
                        certificate.identifier
                    ).run { events.postValue(this) }
                }
            )
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CertificatesViewModel>
}
