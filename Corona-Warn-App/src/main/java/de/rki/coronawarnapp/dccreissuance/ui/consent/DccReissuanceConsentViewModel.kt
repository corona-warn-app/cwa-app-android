package de.rki.coronawarnapp.dccreissuance.ui.consent

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DccReissuanceConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    private val format: CclTextFormatter,
    @Assisted private val personIdentifierCode: String,
    private val personCertificatesSettings: PersonCertificatesSettings,
) : CWAViewModel(dispatcherProvider) {

    val dccReissuanceData = personCertificatesProvider.personCertificates.map { persons ->
        val person = persons.find { it.personIdentifier?.codeSHA256 == personIdentifierCode }!!
        person.personIdentifier?.let { personCertificatesSettings.dismissReissuanceBadge(it) }
        person.dccWalletInfo!!.certificateReissuance
    }.catch {
    }.asLiveData2()

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccReissuanceConsentViewModel> {
        fun create(
            personIdentifierCode: String
        ): DccReissuanceConsentViewModel
    }
}
