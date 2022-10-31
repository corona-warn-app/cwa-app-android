package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.AdmissionState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.MaskState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.ReissuanceDivision
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.VaccinationState
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.person.core.isHighestCertificateDisplayValid
import de.rki.coronawarnapp.covidcertificate.person.core.isMaskOptional
import de.rki.coronawarnapp.covidcertificate.person.core.toIdentifier
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.AdmissionStatusCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.BoosterCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateReissuanceCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.MaskRequirementsCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade.Companion.colorForState
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
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
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    @Assisted private val groupKey: String,
    @Assisted private val colorShade: PersonColorShade,
    private val format: CclTextFormatter,
) : CWAViewModel(dispatcherProvider) {

    private val colorShadeData = MutableLiveData(colorShade)
    val events = SingleLiveEvent<PersonDetailsEvents>()
    val currentColorShade: LiveData<PersonColorShade> = colorShadeData

    private val loadingButtonState = MutableStateFlow(false)
    private val personCertificatesFlow = personCertificatesProvider.personCertificates.mapNotNull { certificateSet ->
        certificateSet.first { it.personIdentifier.belongsToSamePerson(groupKey.toIdentifier()) }
    }.catch { error ->
        Timber.d(error, "No person found for $groupKey")
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

        val color = colorForState(
            validCertificate = personCertificates.isHighestCertificateDisplayValid,
            isMaskOptional = personCertificates.isMaskOptional,
            // Person certificates can change in here, therefore if Mask is required again ignore Blue color shade from
            // PersonOverview
            currentColor = if (personCertificates.isMaskOptional) colorShade else PersonColorShade.COLOR_1
        )
        colorShadeData.postValue(color)

        val certificateItems = mutableListOf<CertificateItem>()
        personCertificates.dccWalletInfo?.let { info ->
            // 1. Mask state tile
            val maskState = info.maskState
            if (maskState?.visible == true) {
                certificateItems.add(maskStateItem(maskState, color))
            }
            // 2. Admission state tile
            if (info.admissionState.visible)
                certificateItems.add(admissionStateItem(info.admissionState, personCertificates, color))
            // 3. Dcc reissuance tile
            if (info.hasReissuance) info.certificateReissuance?.reissuanceDivision?.let { division ->
                certificateItems.add(dccReissuanceItem(division, personCertificates))
            }
            // 4. Booster notification tile
            if (info.boosterNotification.visible)
                certificateItems.add(boosterItem(info.boosterNotification, personCertificates))
            // 5.Vaccination state tile
            if (info.vaccinationState.visible)
                certificateItems.add(vaccinationInfoItem(info.vaccinationState))
        }

        // Person details tile
        certificateItems.add(cwaUserCard(personCertificates))

        // Certificates tiles
        personCertificates.certificates // `certificates` are already sorted by date
            // Sorting by `whether it is high prio certificate` will bring this certificate to the top
            .sortedByDescending { it.containerId == priorityCertificate.containerId }
            .forEach { cwaCert ->
                certificateItems.addCardItem(
                    certificate = cwaCert,
                    priorityCertificate = priorityCertificate,
                    isLoading = isLoading,
                    colorShade = color,
                )
            }

        return UiState(
            name = priorityCertificate.fullName,
            certificateItems = certificateItems,
            numberOfCertificates = personCertificates.certificates.size
        )
    }

    private fun MutableList<CertificateItem>.addCardItem(
        certificate: CwaCovidCertificate,
        priorityCertificate: CwaCovidCertificate,
        isLoading: Boolean,
        colorShade: PersonColorShade,
    ) {
        val isCurrentCertificate = certificate.containerId == priorityCertificate.containerId
        when (certificate) {
            is TestCertificate -> add(
                tcItem(certificate, isCurrentCertificate, isLoading, colorShade)
            )
            is VaccinationCertificate -> add(
                vcItem(certificate, isCurrentCertificate, isLoading, colorShade)
            )
            is RecoveryCertificate -> add(
                rcItem(certificate, isCurrentCertificate, isLoading, colorShade)
            )
        }
    }

    private suspend fun maskStateItem(
        maskState: MaskState,
        colorShade: PersonColorShade
    ) = MaskRequirementsCard.Item(
        titleText = format(maskState.titleText),
        subtitleText = format(maskState.subtitleText),
        maskStateIdentifier = maskState.identifier,
        longText = format(maskState.longText),
        faqAnchor = format(maskState.faqAnchor),
        colorShade = colorShade
    )

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
        personCertificates: PersonCertificates,
        colorShade: PersonColorShade,
    ) = AdmissionStatusCard.Item(
        titleText = format(admissionState.titleText),
        subtitleText = format(admissionState.subtitleText),
        badgeText = format(admissionState.badgeText),
        badgeVisible = personCertificates.hasNewAdmissionState,
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
        onClick = { events.postValue(OpenBoosterInfoDetails(groupKey)) }
    )

    private suspend fun dccReissuanceItem(
        division: ReissuanceDivision,
        personCertificates: PersonCertificates
    ) = CertificateReissuanceCard.Item(
        title = format(division.titleText),
        subtitle = format(division.subtitleText),
        badgeVisible = personCertificates.hasDccReissuanceBadge,
        onClick = { events.postValue(OpenCertificateReissuanceConsent(groupKey)) }
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
        isLoading: Boolean,
        colorShade: PersonColorShade,
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
                    colorShade = getDetailsColorShade(isCurrentCertificate, colorShade)
                )
            )
        },
        onSwipeItem = { swipedCertificate, position ->
            events.postValue(RecycleCertificate(swipedCertificate, position))
        }
    )

    fun recycleCertificate(certificate: CwaCovidCertificate) = launch {
        recycledCertificatesProvider.recycleCertificate(certificate.containerId)
    }

    private fun vcItem(
        certificate: VaccinationCertificate,
        isCurrentCertificate: Boolean,
        isLoading: Boolean,
        colorShade: PersonColorShade,
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
                    colorShade = getDetailsColorShade(isCurrentCertificate, colorShade)
                )
            )
        },
        onSwipeItem = { swipedCertificate, position ->
            events.postValue(RecycleCertificate(swipedCertificate, position))
        }
    )

    private fun tcItem(
        certificate: TestCertificate,
        isCurrentCertificate: Boolean,
        isLoading: Boolean,
        colorShade: PersonColorShade,
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
                    colorShade = getDetailsColorShade(isCurrentCertificate, colorShade)
                )
            )
        },
        onSwipeItem = { swipedCertificate, position ->
            events.postValue(RecycleCertificate(swipedCertificate, position))
        }
    )

    fun dismissAdmissionStateBadge(shouldPopBackstack: Boolean = false) {
        viewModelScope.launch {
            personCertificatesProvider.findPersonByIdentifierCode(groupKey)
                .firstOrNull()?.personIdentifier
                ?.let { personCertificatesSettings.dismissGStatusBadge(it) }
            if (shouldPopBackstack) events.postValue(Back)
        }
    }

    private fun getDetailsColorShade(
        isCurrentCertificate: Boolean,
        colorShade: PersonColorShade
    ): PersonColorShade = when {
        !isCurrentCertificate -> PersonColorShade.COLOR_INVALID
        colorShade == PersonColorShade.GREEN -> PersonColorShade.COLOR_1
        else -> colorShade
    }

    data class UiState(
        val name: String,
        val certificateItems: List<CertificateItem>,
        val numberOfCertificates: Int = 0
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonDetailsViewModel> {
        fun create(
            groupKey: String,
            colorShade: PersonColorShade,
        ): PersonDetailsViewModel
    }
}
