package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.core.toCertificateSortOrder
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificateFilter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingNoValidCertificateCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingNoValidCertificateFaqCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingNoValidCertificateHeaderCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingRecoveryCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingTestCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingVaccinationCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingValidCertificateHeaderCard
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
    val items = dccTicketingSharedViewModel.transactionContext.map { cxt -> certificateItems(cxt) }.asLiveData2()

    private suspend fun certificateItems(cxt: DccTicketingTransactionContext): List<DccTicketingCertificateItem> {
        val validationCondition = cxt.accessTokenPayload?.vc
        val certificates = dccTicketingCertificateFilter.filter(validationCondition)
        return when {
            certificates.isEmpty() -> listOf(
                // Header no Valid certificates
                DccTicketingNoValidCertificateHeaderCard.Item(validationCondition),
                // No valid certificates
                DccTicketingNoValidCertificateCard.Item(validationCondition),
                // FAQ footer
                DccTicketingNoValidCertificateFaqCard.Item(validationCondition)
            )

            else -> {
                mutableListOf<DccTicketingCertificateItem>().apply {
                    // Header of Valid certificates
                    add(DccTicketingValidCertificateHeaderCard.Item(validationCondition))
                    // Valid Certificates
                    addAll(certificates.toCertificateSortOrder().map { it.toCertificateItem() })
                }
            }
        }
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

    fun closeScreen() {
        events.postValue(CloseSelectionScreen)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingCertificateSelectionViewModel> {
        fun create(dccTicketingSharedViewModel: DccTicketingSharedViewModel): DccTicketingCertificateSelectionViewModel
    }
}
