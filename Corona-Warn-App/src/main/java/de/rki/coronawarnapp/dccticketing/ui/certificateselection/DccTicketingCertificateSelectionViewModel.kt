package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificateFilter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

class DccTicketingCertificateSelectionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingCertificateFilter: DccTicketingCertificateFilter,
    @Assisted private val transactionContext: DccTicketingTransactionContext
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<DccTicketingCertificateSelectionEvents>()

    suspend fun getCertificates() =
        transactionContext.accessTokenPayload?.let { dccTicketingCertificateFilter.filter(it) }?.map {
            mapToDccTicketingCertificateItem(it)
        }

    private fun mapToDccTicketingCertificateItem(certificate: CwaCovidCertificate): DccTicketingCertificateItem =
        when (certificate) {
            is TestCertificate -> DccTicketingTestCard.Item(certificate = certificate) {
                events.postValue(NavigateToConsentTwoFragment(transactionContext, certificate.containerId))
            }
            is RecoveryCertificate -> DccTicketingRecoveryCard.Item(certificate = certificate) {
                events.postValue(NavigateToConsentTwoFragment(transactionContext, certificate.containerId))
            }
            is VaccinationCertificate -> DccTicketingVaccinationCard.Item(certificate = certificate) {
                events.postValue(NavigateToConsentTwoFragment(transactionContext, certificate.containerId))
            }
            else -> DccTicketingNoValidCertificateCard.Item
        }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingCertificateSelectionViewModel> {
        fun create(transactionContext: DccTicketingTransactionContext): DccTicketingCertificateSelectionViewModel
    }
}
