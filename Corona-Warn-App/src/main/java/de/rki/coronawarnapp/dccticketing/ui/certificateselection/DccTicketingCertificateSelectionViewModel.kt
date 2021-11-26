package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificateFilter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DccTicketingCertificateSelectionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingCertificateFilter: DccTicketingCertificateFilter,
    @Assisted private val dccTicketingSharedViewModel: DccTicketingSharedViewModel,
) : CWAViewModel(dispatcherProvider) {

    private val currentTransactionContext = dccTicketingSharedViewModel.transactionContext
        .map { UiState(it) }
    val uiState: LiveData<UiState> = currentTransactionContext.asLiveData2()
    val events = SingleLiveEvent<DccTicketingCertificateSelectionEvents>()

    suspend fun getCertificates() =
        dccTicketingCertificateFilter.filter(currentTransactionContext.first().validationCondition).map { certificate ->
            mapToDccTicketingCertificateItem(certificate, currentTransactionContext.first().validationCondition)
        }

    private fun mapToDccTicketingCertificateItem(
        certificate: CwaCovidCertificate,
        validationCondition: DccTicketingValidationCondition?
    ): DccTicketingCertificateItem =
        when (certificate) {
            is TestCertificate -> DccTicketingTestCard.Item(certificate = certificate) {
                events.postValue(NavigateToConsentTwoFragment(certificate.containerId))
            }
            is RecoveryCertificate -> DccTicketingRecoveryCard.Item(certificate = certificate) {
                events.postValue(NavigateToConsentTwoFragment(certificate.containerId))
            }
            is VaccinationCertificate -> DccTicketingVaccinationCard.Item(certificate = certificate) {
                events.postValue(NavigateToConsentTwoFragment(certificate.containerId))
            }
            else -> DccTicketingNoValidCertificateCard.Item(validationCondition = validationCondition)
        }

    data class UiState(
        private val dccTicketingTransactionContext: DccTicketingTransactionContext
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
