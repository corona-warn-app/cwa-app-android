package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccadmission.calculation.DccAdmissionCheckScenariosCalculation
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationNotificationService
import de.rki.coronawarnapp.covidcertificate.person.core.MigrationCheck
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosSharedViewModel
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard.Item.OverviewCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

@Suppress("LongParameterList")
class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    dccAdmissionTileProvider: AdmissionTileProvider,
    @Assisted private val admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
    @AppScope private val appScope: CoroutineScope,
    private val testCertificateRepository: TestCertificateRepository,
    private val expirationNotificationService: DccExpirationNotificationService,
    private val format: CclTextFormatter,
    private val admissionCheckScenariosCalculation: DccAdmissionCheckScenariosCalculation,
    private val migrationCheck: MigrationCheck
) : CWAViewModel(dispatcherProvider) {

    val admissionTile = dccAdmissionTileProvider.admissionTile.asLiveData2()
    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val uiState: LiveData<UiState> = combine<Set<PersonCertificates>, Set<TestCertificateWrapper>, UiState>(
        certificatesProvider.personCertificates,
        testCertificateRepository.certificates,
    ) { persons, tcWrappers ->

        if (migrationCheck.shouldShowMigrationInfo(persons)) {
            events.postValue(ShowMigrationInfoDialog)
        }

        UiState.Done(
            mutableListOf<PersonCertificatesItem>().apply {
                addPersonItems(persons, tcWrappers)
            }
        )
    }.onStart { emit(UiState.Loading) }.asLiveData2()

    fun deleteTestCertificate(containerId: TestCertificateContainerId) = launch {
        testCertificateRepository.deleteCertificate(containerId)
    }

    private suspend fun MutableList<PersonCertificatesItem>.addPersonItems(
        persons: Set<PersonCertificates>,
        tcWrappers: Set<TestCertificateWrapper>,
    ) {
        addPendingCards(tcWrappers)
        addCertificateCards(persons)
    }

    private suspend fun MutableList<PersonCertificatesItem>.addCertificateCards(
        persons: Set<PersonCertificates>,
    ) {
        persons.filterNotPending().forEachIndexed { index, person ->
            val admissionState = person.dccWalletInfo?.admissionState
            val certificates = person.verificationCertificates
            val color = PersonColorShade.shadeFor(index)
            if (certificates.isNotEmpty()) {
                add(
                    PersonCertificateCard.Item(
                        overviewCertificates = certificates.map {
                            OverviewCertificate(it.cwaCertificate, format(it.buttonText))
                        },
                        admissionBadgeText = format(admissionState?.badgeText),
                        colorShade = color,
                        badgeCount = person.badgeCount,
                        onClickAction = { _, position ->
                            person.personIdentifier?.let { personIdentifier ->
                                events.postValue(
                                    OpenPersonDetailsFragment(personIdentifier.codeSHA256, position, color)
                                )
                            }
                        },
                        onCovPassInfoAction = { events.postValue(OpenCovPassInfo) }
                    )
                )
            }
        }
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

    fun checkExpiration() = launch(scope = appScope) {
        Timber.tag(TAG).d("checkExpiration()")
        expirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
    }

    fun openAdmissionScenarioScreen() = launch {
        runCatching { admissionCheckScenariosCalculation.getDccAdmissionCheckScenarios() }
            .onFailure { events.postValue(ShowAdmissionScenarioError(it)) }
            .onSuccess {
                admissionScenariosSharedViewModel.setAdmissionScenarios(it)
                events.postValue(OpenAdmissionScenarioScreen)
            }
    }

    sealed class UiState {
        object Loading : UiState()
        data class Done(val personCertificates: List<PersonCertificatesItem>) : UiState()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonOverviewViewModel> {
        fun create(
            admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel
        ): PersonOverviewViewModel
    }

    companion object {
        private const val TAG = "PersonOverviewViewModel"
    }
}
