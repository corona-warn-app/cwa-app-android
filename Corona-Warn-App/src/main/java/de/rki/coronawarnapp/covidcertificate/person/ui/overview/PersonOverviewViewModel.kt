package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val testCertificateRepository: TestCertificateRepository
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val personCertificates = certificatesProvider
        .personCertificates
        .asLiveData(dispatcherProvider.Default)

    private fun refreshCertificate(identifier: TestCertificateIdentifier) =
        launch {
            val error = testCertificateRepository.refresh(identifier).mapNotNull { it.error }.singleOrNull()
            error?.let { events.postValue(ShowRefreshErrorDialog(error)) }
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PersonOverviewViewModel>
}
