package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationNotificationService
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val testCertificateRepository: TestCertificateRepository,
    @AppScope private val appScope: CoroutineScope,
    private val expirationNotificationService: DccExpirationNotificationService
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val personCertificates: LiveData<List<PersonCertificatesItem>> = combine(
        certificatesProvider.personCertificates,
        testCertificateRepository.certificates,
    ) { persons, tcWrappers ->
        Timber.tag(TAG).d("persons=%s, tcWrappers=%s", persons, tcWrappers)

        mutableListOf<PersonCertificatesItem>().apply {
            addPersonItems(persons, tcWrappers)
        }
    }.asLiveData(dispatcherProvider.Default)

    fun deleteTestCertificate(containerId: TestCertificateContainerId) = launch {
        testCertificateRepository.deleteCertificate(containerId)
    }

    private fun MutableList<PersonCertificatesItem>.addPersonItems(
        persons: Set<PersonCertificates>,
        tcWrappers: Set<TestCertificateWrapper>,
    ) {
        addPendingCards(tcWrappers)
        addCertificateCards(persons)
    }

    private fun MutableList<PersonCertificatesItem>.addCertificateCards(
        persons: Set<PersonCertificates>,
    ) {
        persons.filterNotPending()
            .forEachIndexed { index, person ->
                val certificate = person.highestPriorityCertificate
                val badgeCount = person.badgeCount
                val color = PersonColorShade.shadeFor(index)
                if (certificate != null) {
                    add(
                        PersonCertificateCard.Item(
                            certificate = certificate,
                            colorShade = color,
                            badgeCount = badgeCount,
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
        val error = testCertificateRepository.refresh(containerId).mapNotNull { it.error }.singleOrNull()
        error?.let { events.postValue(ShowRefreshErrorDialog(error)) }
    }

    fun checkExpiration() = launch(scope = appScope) {
        Timber.d("checkExpiration()")
        expirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PersonOverviewViewModel>

    companion object {
        private const val TAG = "PersonOverviewViewModel"
    }
}
