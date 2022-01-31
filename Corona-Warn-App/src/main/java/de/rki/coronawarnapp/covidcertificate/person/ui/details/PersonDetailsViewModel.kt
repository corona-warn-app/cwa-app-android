package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.Other
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.BoosterCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.ConfirmedStatusCard
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
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

@Suppress("LongParameterList")
class PersonDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val vaccinationRepository: VaccinationRepository,
    private val dccValidationRepository: DccValidationRepository,
    private val timeStamper: TimeStamper,
    @AppScope private val appScope: CoroutineScope,
    @Assisted private val personIdentifierCode: String,
    @Assisted private val colorShade: PersonColorShade
) : CWAViewModel(dispatcherProvider) {

    private val colorShadeData = MutableLiveData(colorShade)
    val events = SingleLiveEvent<PersonDetailsEvents>()
    val currentColorShade: LiveData<PersonColorShade> = colorShadeData

    private val loadingButtonState = MutableStateFlow(false)
    private val personCertificatesFlow = personCertificatesProvider.personCertificates.mapNotNull { certificateSet ->
        certificateSet.first { it.personIdentifier?.codeSHA256 == personIdentifierCode }
    }.catch { error ->
        Timber.d(error, "No person found for $personIdentifierCode")
        events.postValue(Back)
    }

    val uiState: LiveData<UiState> = combine(
        personCertificatesFlow,
        loadingButtonState
    ) { personSpecificCertificates, isLoading ->
        createUiState(personSpecificCertificates, isLoading)
    }.asLiveData2()

    @Suppress("NestedBlockDepth")
    private suspend fun createUiState(personCertificates: PersonCertificates, isLoading: Boolean): UiState {
        val priorityCertificate = personCertificates.highestPriorityCertificate
        if (priorityCertificate == null) {
            events.postValue(Back)
            return UiState(name = "", emptyList())
        }

        val certificateItems = mutableListOf<CertificateItem>().apply {
            when {
                priorityCertificate.isDisplayValid -> colorShade
                else -> PersonColorShade.COLOR_INVALID
            }.also { colorShadeData.postValue(it) }

            add(
                PersonDetailsQrCard.Item(
                    priorityCertificate,
                    isLoading,
                    validateCertificate = { onValidateCertificate(it) },
                    onCovPassInfoAction = { events.postValue(OpenCovPassInfo) }
                )
            )

            val boosterNotification = personCertificates.dccWalletInfoWrapper.dccWalletInfo.boosterNotification
            if (boosterNotification.visible) {
                add(
                    BoosterCard.Item(
                        boosterNotification = boosterNotification,
                        isNew = checkBoosterNotificationBadge(personCertificates, boosterNotification),
                        onClick = { events.postValue(OpenBoosterInfoDetails(personIdentifierCode)) }
                    )
                )
            }

            val admissionState = personCertificates.admissionState
            if (admissionState != null && admissionState !is Other) {
                add(
                    ConfirmedStatusCard.Item(
                        admissionState = admissionState,
                        colorShade = colorShade
                    )
                )
            }

            // Find any vaccination certificate to determine the vaccination information
            personCertificates.certificates.find { it is VaccinationCertificate }?.let { certificate ->
                val vaccinatedPerson = vaccinatedPerson(certificate)
                if (vaccinatedPerson != null) {
                    try {
                        val daysUntilImmunity = vaccinatedPerson.getDaysUntilImmunity()
                        val vaccinationStatus = vaccinatedPerson.getVaccinationStatus()
                        val daysSinceLastVaccination = vaccinatedPerson.getDaysSinceLastVaccination()
                        val boosterRule = vaccinatedPerson.boosterRule
                        add(
                            VaccinationInfoCard.Item(
                                vaccinationStatus = vaccinationStatus,
                                daysUntilImmunity = daysUntilImmunity,
                                boosterRule = boosterRule,
                                daysSinceLastVaccination = daysSinceLastVaccination,
                                hasBoosterNotification = vaccinatedPerson.hasBoosterNotification
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "creating VaccinationInfoCard.Item failed")
                    }
                }
            }

            add(cwaUserCard(personCertificates))

            personCertificates.certificates.forEach { addCardItem(it, priorityCertificate) }
        }

        return UiState(name = priorityCertificate.fullName, certificateItems = certificateItems)
    }

    private suspend fun checkBoosterNotificationBadge(
        personCertificates: PersonCertificates,
        boosterNotification: BoosterNotification
    ): Boolean {
        personCertificates.certificates.find { it is VaccinationCertificate }?.let { certificate ->
            val vaccinatedPerson = vaccinatedPerson(certificate)
            if (vaccinatedPerson?.data?.lastSeenBoosterRuleIdentifier != boosterNotification.identifier) {
                return true
            }
        }
        return false
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
                    events.postValue(
                        OpenTestCertificateDetails(
                            containerId = certificate.containerId,
                            colorShade = getItemColorShade(certificate.isDisplayValid, isCurrentCertificate)
                        )
                    )
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
                        events.postValue(
                            OpenVaccinationCertificateDetails(
                                containerId = certificate.containerId,
                                colorShade = getItemColorShade(certificate.isDisplayValid, isCurrentCertificate)
                            )
                        )
                    }
                )
            }

            is RecoveryCertificate -> add(
                RecoveryCertificateCard.Item(certificate, isCurrentCertificate, colorShade) {
                    events.postValue(
                        OpenRecoveryCertificateDetails(
                            containerId = certificate.containerId,
                            colorShade = getItemColorShade(certificate.isDisplayValid, isCurrentCertificate)
                        )
                    )
                }
            )
        }
    }

    private fun getItemColorShade(isValid: Boolean, isCurrentCertificate: Boolean): PersonColorShade = when {
        isValid && isCurrentCertificate -> colorShade
        else -> PersonColorShade.COLOR_INVALID
    }

    private suspend fun vaccinatedPerson(certificate: CwaCovidCertificate): VaccinatedPerson? =
        vaccinationRepository.vaccinationInfos.firstOrNull()?.find { it.identifier == certificate.personIdentifier }

    data class UiState(
        val name: String,
        val certificateItems: List<CertificateItem>
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonDetailsViewModel> {
        fun create(
            personIdentifierCode: String,
            colorShade: PersonColorShade,
        ): PersonDetailsViewModel
    }
}
