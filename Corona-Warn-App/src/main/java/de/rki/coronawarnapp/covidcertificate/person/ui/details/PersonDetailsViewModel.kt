package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.AdmissionState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.ReissuanceDivision
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.VaccinationState
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.AdmissionStatusCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.BoosterCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateReissuanceCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("LongParameterList")
class PersonDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val personCertificatesSettings: PersonCertificatesSettings,
    private val dccValidationRepository: DccValidationRepository,
    @Assisted private val personIdentifierCode: String,
    @Assisted private val colorShade: PersonColorShade,
    private val format: CclTextFormatter,
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

    private suspend fun createUiState(personCertificates: PersonCertificates, isLoading: Boolean): UiState {
        val priorityCertificate = personCertificates.highestPriorityCertificate
        if (priorityCertificate == null) {
            events.postValue(Back)
            return UiState(name = "", emptyList())
        }

        val dccWalletInfo = personCertificates.dccWalletInfo
        val certificateItems = mutableListOf<CertificateItem>().apply {
            val color = if (priorityCertificate.isDisplayValid) colorShade else PersonColorShade.COLOR_INVALID
            colorShadeData.postValue(color)
            // 1. Admission state tile
            dccWalletInfo?.admissionState?.let { admissionState ->
                if (admissionState.visible) add(admissionStateItem(admissionState, personCertificates))
            }
            // 2. Dcc reissuance tile
            dccWalletInfo?.certificateReissuance?.reissuanceDivision?.let { division ->
                if (division.visible) add(dccReissuanceItem(division, personCertificates))
            }
            // 3. Booster notification tile
            dccWalletInfo?.boosterNotification?.let { boosterNotification ->
                if (boosterNotification.visible) add(boosterItem(boosterNotification, personCertificates))
            }
            // 4.Vaccination state tile
            dccWalletInfo?.vaccinationState?.let { vaccinationState ->
                if (vaccinationState.visible) add(vaccinationInfoItem(vaccinationState))
            }
            // Person details tile
            add(cwaUserCard(personCertificates))
            // Certificates tiles
            personCertificates.certificates.forEach {
                addCardItem(
                    certificate = it,
                    priorityCertificate = priorityCertificate,
                    isLoading = isLoading
                )
            }
        }

        return UiState(
            name = priorityCertificate.fullName,
            certificateItems = certificateItems
        )
    }

    private fun MutableList<CertificateItem>.addCardItem(
        certificate: CwaCovidCertificate,
        priorityCertificate: CwaCovidCertificate,
        isLoading: Boolean
    ) {
        val isCurrentCertificate = certificate.containerId == priorityCertificate.containerId
        when (certificate) {
            is TestCertificate -> add(tcItem(certificate, isCurrentCertificate, isLoading))
            is VaccinationCertificate -> add(vcItem(certificate, isCurrentCertificate, isLoading))
            is RecoveryCertificate -> add(rcItem(certificate, isCurrentCertificate, isLoading))
        }
    }

    private suspend fun vaccinationInfoItem(
        vaccinationState: VaccinationState
    ) = VaccinationInfoCard.Item(
        titleText = format(vaccinationState.titleText),
        subtitleText = format(vaccinationState.subtitleText),
        longText = format(vaccinationState.longText),
        faqAnchor = format(vaccinationState.faqAnchor),
    )

    private suspend fun admissionStateItem(
        admissionState: AdmissionState,
        personCertificates: PersonCertificates
    ) = AdmissionStatusCard.Item(
        titleText = format(admissionState.titleText),
        subtitleText = format(admissionState.subtitleText),
        badgeText = format(admissionState.badgeText),
        redBadgeVisible = personCertificates.hasNewAdmissionState,
        longText = format(admissionState.longText),
        longTextWithBadge = format(admissionState.stateChangeNotificationText),
        faqAnchor = format(admissionState.faqAnchor),
        colorShade = colorShade
    )

    private suspend fun boosterItem(
        boosterNotification: BoosterNotification,
        personCertificates: PersonCertificates
    ) = BoosterCard.Item(
        title = format(boosterNotification.titleText),
        subtitle = format(boosterNotification.subtitleText),
        badgeVisible = personCertificates.hasBoosterBadge,
        onClick = { events.postValue(OpenBoosterInfoDetails(personIdentifierCode)) }
    )

    private suspend fun dccReissuanceItem(
        division: ReissuanceDivision,
        personCertificates: PersonCertificates
    ) = CertificateReissuanceCard.Item(
        title = format(division.titleText),
        subtitle = format(division.subtitleText),
        badgeVisible = personCertificates.hasDccReissuanceBadge,
        onClick = { events.postValue(OpenCertificateReissuanceConsent(personIdentifierCode)) }
    )

    private fun onValidateCertificate(containerId: CertificateContainerId) = launch {
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

    private fun rcItem(
        certificate: RecoveryCertificate,
        isCurrentCertificate: Boolean,
        isLoading: Boolean
    ) = RecoveryCertificateCard.Item(
        certificate = certificate,
        isCurrentCertificate = isCurrentCertificate,
        colorShade = colorShade,
        isLoading = isLoading,
        validateCertificate = { onValidateCertificate(it) },
        onClick = {
            events.postValue(
                OpenRecoveryCertificateDetails(
                    containerId = certificate.containerId,
                    colorShade = getItemColorShade(certificate.isDisplayValid, isCurrentCertificate)
                )
            )
        }
    )

    private fun vcItem(
        certificate: VaccinationCertificate,
        isCurrentCertificate: Boolean,
        isLoading: Boolean
    ) = VaccinationCertificateCard.Item(
        certificate = certificate,
        isCurrentCertificate = isCurrentCertificate,
        colorShade = colorShade,
        isLoading = isLoading,
        validateCertificate = { onValidateCertificate(it) },
        onClick = {
            events.postValue(
                OpenVaccinationCertificateDetails(
                    containerId = certificate.containerId,
                    colorShade = getItemColorShade(certificate.isDisplayValid, isCurrentCertificate)
                )
            )
        }
    )

    private fun tcItem(
        certificate: TestCertificate,
        isCurrentCertificate: Boolean,
        isLoading: Boolean
    ) = TestCertificateCard.Item(
        certificate = certificate,
        isCurrentCertificate = isCurrentCertificate,
        colorShade = colorShade,
        isLoading = isLoading,
        validateCertificate = { onValidateCertificate(it) },
        onClick = {
            events.postValue(
                OpenTestCertificateDetails(
                    containerId = certificate.containerId,
                    colorShade = getItemColorShade(certificate.isDisplayValid, isCurrentCertificate)
                )
            )
        }
    )

    fun dismissAdmissionStateBadge() {
        viewModelScope.launch {
            personCertificatesProvider.findPersonByIdentifierCode(personIdentifierCode)
                .firstOrNull()?.personIdentifier
                ?.let { personCertificatesSettings.dismissGStatusBadge(it) }
            events.postValue(Back)
        }
    }

    private fun getItemColorShade(
        isValid: Boolean,
        isCurrentCertificate: Boolean
    ): PersonColorShade = when {
        isValid && isCurrentCertificate -> colorShade
        else -> PersonColorShade.COLOR_INVALID
    }

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
