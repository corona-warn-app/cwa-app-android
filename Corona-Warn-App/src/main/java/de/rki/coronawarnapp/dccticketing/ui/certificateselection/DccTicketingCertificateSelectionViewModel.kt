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
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map

class DccTicketingCertificateSelectionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingCertificateFilter: DccTicketingCertificateFilter,
    @Assisted private val dccTicketingSharedViewModel: DccTicketingSharedViewModel,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<DccTicketingCertificateSelectionEvents>()
    val uiState = dccTicketingSharedViewModel.transactionContext.map { cxt ->
        uiState(cxt)
    }.asLiveData2()

    private suspend fun uiState(cxt: DccTicketingTransactionContext): UiState {
        val certificates = dccTicketingCertificateFilter.filter(cxt.accessTokenPayload?.vc)
        val certificateItems = when {
            certificates.isEmpty() -> listOf(
                DccTicketingNoValidCertificateCard.Item(validationCondition = cxt.accessTokenPayload?.vc)
            )

            else -> certificates.map { it.toCertificateItem() }
        }
        return UiState(
            dccTicketingTransactionContext = cxt,
            certificateItems = certificateItems
        )
    }

    private fun CwaCovidCertificate.toCertificateItem(): DccTicketingCertificateItem =
        when (this) {
            is TestCertificate -> DccTicketingTestCard.Item(certificate = this) {
                events.postValue(NavigateToConsentTwoFragment(containerId))
            }
            is RecoveryCertificate -> DccTicketingRecoveryCard.Item(certificate = this) {
                events.postValue(NavigateToConsentTwoFragment(containerId))
            }
            is VaccinationCertificate -> DccTicketingVaccinationCard.Item(certificate = this) {
                events.postValue(NavigateToConsentTwoFragment(containerId))
            }
            else -> error("Unsupported certificate$this")
        }

    data class UiState(
        private val dccTicketingTransactionContext: DccTicketingTransactionContext,
        val certificateItems: List<DccTicketingCertificateItem>
    ) {
        val validationCondition get() = dccTicketingTransactionContext.accessTokenPayload?.vc
    }

    fun closeScreen() {
        events.postValue(CloseSelectionScreen)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingCertificateSelectionViewModel> {
        fun create(dccTicketingSharedViewModel: DccTicketingSharedViewModel): DccTicketingCertificateSelectionViewModel
    }
}
