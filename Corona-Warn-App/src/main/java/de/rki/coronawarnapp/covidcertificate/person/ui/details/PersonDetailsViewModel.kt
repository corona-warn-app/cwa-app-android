package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.PersonDetailsQrCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

@Suppress("LongParameterList")
class PersonDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val vaccinationRepository: VaccinationRepository,
    private val dccValidationRepository: DccValidationRepository,
    private val timeStamper: TimeStamper,
    @Assisted private val personIdentifierCode: String,
    @Assisted private val colorShade: PersonColorShade
) : CWAViewModel(dispatcherProvider) {

    private val colorShadeData = MutableLiveData(colorShade)
    val events = SingleLiveEvent<PersonDetailsEvents>()
    val currentColorShade: LiveData<PersonColorShade> = colorShadeData

    private val loadingButtonState = MutableStateFlow(false)
    private val personCertificatesFlow = personCertificatesProvider.personCertificates.mapNotNull { certificateSet ->
        certificateSet.first { it.personIdentifier.codeSHA256 == personIdentifierCode }
    }.catch { error ->
        Timber.d(error, "No person found for $personIdentifierCode")
        events.postValue(Back)
    }

    val uiState: LiveData<List<CertificateItem>> = combine(
        personCertificatesFlow,
        loadingButtonState
    ) { personSpecificCertificates, isLoading ->
        assembleList(personSpecificCertificates, isLoading)
    }.asLiveData2()

    private suspend fun assembleList(personCertificates: PersonCertificates, isLoading: Boolean) =
        mutableListOf<CertificateItem>().apply {
            val priorityCertificate = personCertificates.highestPriorityCertificate

            when {
                priorityCertificate.isValid -> colorShade
                else -> PersonColorShade.COLOR_INVALID
            }.also { colorShadeData.postValue(it) }

            add(
                PersonDetailsQrCard.Item(priorityCertificate, isLoading) { onValidateCertificate(it) }
            )
            add(cwaUserCard(personCertificates))

            // Find any vaccination certificate to determine the vaccination information
            personCertificates.certificates.find { it is VaccinationCertificate }?.let { certificate ->
                val vaccinatedPerson = vaccinatedPerson(certificate)
                if (vaccinatedPerson != null && vaccinatedPerson.getVaccinationStatus(timeStamper.nowUTC) != IMMUNITY) {
                    val timeUntilImmunity = vaccinatedPerson.getDaysUntilImmunity()
                    add(VaccinationInfoCard.Item(timeUntilImmunity))
                }
            }

            personCertificates.certificates.forEach { addCardItem(it, personCertificates.highestPriorityCertificate) }
        }

    private fun onValidateCertificate(containerId: CertificateContainerId) =
        launch {
            try {
                loadingButtonState.value = true
                dccValidationRepository.refresh()
                events.postValue(ValidationStart(containerId))
            } catch (e: Exception) {
                Timber.d(e, "Validation start failed for containerId=%s", containerId)
                events.postValue(ShowErrorDialog(e))
            } finally {
                loadingButtonState.value = false
            }
        }

    private fun cwaUserCard(
        personCertificates: PersonCertificates
    ) = CwaUserCard.Item(personCertificates) { checked ->
        launch {
            val identifier = if (checked) personCertificates.personIdentifier else null
            personCertificatesProvider.setCurrentCwaUser(identifier)
        }
    }

    private suspend fun MutableList<CertificateItem>.addCardItem(
        certificate: CwaCovidCertificate,
        priorityCertificate: CwaCovidCertificate
    ) {
        val isCurrentCertificate = certificate.containerId == priorityCertificate.containerId
        when (certificate) {
            is TestCertificate -> add(
                TestCertificateCard.Item(certificate, isCurrentCertificate, colorShade) {
                    events.postValue(OpenTestCertificateDetails(certificate.containerId))
                }
            )
            is VaccinationCertificate -> {
                val status = vaccinatedPerson(certificate)?.getVaccinationStatus(timeStamper.nowUTC) ?: INCOMPLETE
                add(
                    VaccinationCertificateCard.Item(
                        certificate = certificate,
                        isCurrentCertificate = isCurrentCertificate,
                        colorShade = colorShade,
                        status = status
                    ) {
                        events.postValue(OpenVaccinationCertificateDetails(certificate.containerId))
                    }
                )
            }

            is RecoveryCertificate -> add(
                RecoveryCertificateCard.Item(certificate, isCurrentCertificate, colorShade) {
                    events.postValue(OpenRecoveryCertificateDetails(certificate.containerId))
                }
            )
        }
    }

    private suspend fun vaccinatedPerson(certificate: CwaCovidCertificate): VaccinatedPerson? =
        vaccinationRepository.vaccinationInfos.first().find { it.identifier == certificate.personIdentifier }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonDetailsViewModel> {
        fun create(
            personIdentifierCode: String,
            colorShade: PersonColorShade,
        ): PersonDetailsViewModel
    }
}
