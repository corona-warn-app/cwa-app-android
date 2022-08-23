package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccadmission.calculation.DccAdmissionCheckScenariosCalculation
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.MigrationCheck
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.isHighestCertificateDisplayValid
import de.rki.coronawarnapp.covidcertificate.person.core.isMaskOptional
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosSharedViewModel
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade.Companion.colorForState
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade.Companion.shadeFor
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard.Item.CertificateSelection
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard.Item.OverviewCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.mutate
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

@Suppress("LongParameterList")
class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    dccAdmissionTileProvider: AdmissionTileProvider,
    @Assisted private val admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
    @Assisted private val savedState: SavedStateHandle,
    @AppScope private val appScope: CoroutineScope,
    private val testCertificateRepository: TestCertificateRepository,
    private val format: CclTextFormatter,
    private val admissionCheckScenariosCalculation: DccAdmissionCheckScenariosCalculation,
    private val migrationCheck: MigrationCheck,
    private val onboardingSettings: OnboardingSettings,
) : CWAViewModel(dispatcherProvider) {

    private val selectedCertificates = MutableStateFlow(
        savedState.get<Map<String, CertificateSelection>>(SELECTIONS_KEY).orEmpty()
    )
    val admissionTile = dccAdmissionTileProvider.admissionTile.asLiveData2()

    val isExportAllTooltipVisible = combine(
        onboardingSettings.exportAllOnboardingDone,
        certificatesProvider.personCertificates
    ) { done, personCerts ->
        !done && personCerts.isNotEmpty()
    }.asLiveData2()
    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val uiState: LiveData<UiState> = combine<Set<PersonCertificates>,
        Set<TestCertificateWrapper>,
        Map<String, CertificateSelection>,
        UiState>(
        certificatesProvider.personCertificates,
        testCertificateRepository.certificates,
        selectedCertificates
    ) { persons, tcWrappers, selections ->

        if (migrationCheck.shouldShowMigrationInfo(persons)) {
            events.postValue(ShowMigrationInfoDialog)
        }

        UiState.Done(
            mutableListOf<PersonCertificatesItem>().apply {
                addPersonItems(persons, tcWrappers, selections)
            }
        )
    }.onStart { emit(UiState.Loading) }.asLiveData2()

    fun deleteTestCertificate(containerId: TestCertificateContainerId) = launch {
        testCertificateRepository.deleteCertificate(containerId)
    }

    private suspend fun MutableList<PersonCertificatesItem>.addPersonItems(
        persons: Set<PersonCertificates>,
        tcWrappers: Set<TestCertificateWrapper>,
        selections: Map<String, CertificateSelection>,
    ) {
        addPendingCards(tcWrappers)
        addAll(persons.toCertificatesCard(selections))
    }

    private suspend fun Set<PersonCertificates>.toCertificatesCard(
        selections: Map<String, CertificateSelection>
    ) = this
        .filterNotPending()
        .filterNot { it.certificates.isEmpty() }
        .mapIndexed { index, person ->
            val admissionState = person.dccWalletInfo?.admissionState
            val certificates = person.verificationCertificates
            val color = colorForState(
                validCertificate = person.isHighestCertificateDisplayValid,
                isMaskOptional = person.isMaskOptional,
                currentColor = shadeFor(index)
            )

            PersonCertificateCard.Item(
                overviewCertificates = certificates.map {
                    OverviewCertificate(it.cwaCertificate, format(it.buttonText))
                },
                admissionBadgeText = format(admissionState?.badgeText),
                colorShade = color,
                badgeCount = person.badgeCount,
                certificateSelection = selections[person.personIdentifier.groupingKey] ?: CertificateSelection.FIRST,
                onClickAction = { _, position ->
                    person.personIdentifier.let { personIdentifier ->
                        events.postValue(
                            OpenPersonDetailsFragment(personIdentifier.codeSHA256, position, color)
                        )
                    }
                },
                onCovPassInfoAction = { events.postValue(OpenCovPassInfo) },
                onCertificateSelected = { selection ->
                    selectedCertificates.update { prevSelections ->
                        prevSelections.mutate {
                            put(person.personIdentifier.groupingKey, selection)
                        }.also {
                            savedState[SELECTIONS_KEY] = it
                        }
                    }
                }
            )
        }

    private fun MutableList<PersonCertificatesItem>.addPendingCards(tcWrappers: Set<TestCertificateWrapper>) {
        tcWrappers.filter {
            it.isCertificateRetrievalPending
        }.forEach { certificateWrapper ->
            add(
                CovidTestCertificatePendingCard.Item(
                    certificate = certificateWrapper,
                    onRetryAction = { refreshCertificate(certificateWrapper.containerId) },
                    onDeleteAction = { events.postValue(ShowDeleteDialog(certificateWrapper.containerId)) }
                )
            )
        }
    }

    private fun PersonCertificates.hasPendingTestCertificate(): Boolean {
        val certificate = highestPriorityCertificate
        return certificate is TestCertificate && certificate.isCertificateRetrievalPending
    }

    private fun Set<PersonCertificates>.filterNotPending() = this
        .filter { !it.hasPendingTestCertificate() }
        .sortedBy { it.highestPriorityCertificate?.fullName }
        .sortedByDescending { it.isCwaUser }

    fun refreshCertificate(containerId: TestCertificateContainerId) = launch(scope = appScope) {
        val refreshResults = testCertificateRepository.refresh(containerId)
        val error = refreshResults.mapNotNull { it.error }.singleOrNull()
        error?.let { events.postValue(ShowRefreshErrorDialog(error)) }
    }

    fun openAdmissionScenarioScreen() = launch {
        runCatching { admissionCheckScenariosCalculation.getDccAdmissionCheckScenarios() }
            .onFailure { events.postValue(ShowAdmissionScenarioError(it)) }
            .onSuccess {
                admissionScenariosSharedViewModel.setAdmissionScenarios(it)
                events.postValue(OpenAdmissionScenarioScreen)
            }
    }

    fun dismissExportAllToolTip() = launch {
        onboardingSettings.updateExportAllOnboardingDone(isDone = true)
    }

    sealed class UiState {
        object Loading : UiState()
        data class Done(val personCertificates: List<PersonCertificatesItem>) : UiState()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonOverviewViewModel> {
        fun create(
            admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
            savedState: SavedStateHandle,
        ): PersonOverviewViewModel
    }

    companion object {
        private const val SELECTIONS_KEY = "PersonOverviewViewModel.SELECTIONS_KEY"
    }
}
