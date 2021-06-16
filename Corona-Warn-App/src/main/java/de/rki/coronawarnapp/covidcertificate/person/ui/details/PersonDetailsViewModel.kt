package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.mapNotNull

class PersonDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val testCertificateRepository: TestCertificateRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    @Assisted private val personIdentifierCode: String
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<PersonDetailsEvents>()

    val personSpecificCertificates: LiveData<PersonCertificates> =
        certificatesProvider.personCertificates.mapNotNull { certificateSet ->
            certificateSet
                .firstOrNull { it.personIdentifier.codeSHA256 == personIdentifierCode }
        }.asLiveData(dispatcherProvider.Default).also { it.value }


    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonDetailsViewModel> {
        fun create(
            personIdentifierCode: String
        ): PersonDetailsViewModel
    }
}
