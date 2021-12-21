package de.rki.coronawarnapp.dccticketing.ui.consent.two

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.submission.DccTicketingSubmissionHandler
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingRecoveryCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingTestCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingVaccinationCard
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DccTicketingConsentTwoViewModel @AssistedInject constructor(
    @Assisted private val dccTicketingSharedViewModel: DccTicketingSharedViewModel,
    @Assisted private val containerId: CertificateContainerId,
    private val certificateProvider: CertificateProvider,
    private val dccTicketingSubmissionHandler: DccTicketingSubmissionHandler,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentIsLoading = MutableStateFlow(false)
    val isLoading: LiveData<Boolean> = currentIsLoading.asLiveData()

    private val currentEvent = SingleLiveEvent<DccTicketingConsentTwoEvent>()
    val events: LiveData<DccTicketingConsentTwoEvent> = currentEvent

    private val uiStateFlow = dccTicketingSharedViewModel.transactionContext.map { cxt ->
        UiState(
            dccTicketingTransactionContext = cxt,
            certificate = certificateProvider.findCertificate(containerId)
        )
    }
    val uiState = uiStateFlow.asLiveData2()

    fun showCancelConfirmationDialog() {
        Timber.d("showCancelConfirmationDialog()")
        postEvent(ShowCancelConfirmationDialog)
    }

    fun goBack() = postEvent(NavigateBack)

    fun cancel() = postEvent(NavigateToHome)

    fun showPrivacyInformation() = postEvent(NavigateToPrivacyInformation)

    fun onUserConsent(): Unit = launch {
        Timber.d("onUserConsent()")
        currentIsLoading.compareAndSet(expect = false, update = true)
        val currentState = uiStateFlow.first()
        val event = try {
            dccTicketingSharedViewModel.updateTransactionContext {
                val ctx = currentState.dccTicketingTransactionContext.copy(
                    dccBarcodeData = currentState.certificate.qrCodeToDisplay.content
                )
                dccTicketingSubmissionHandler.submitDcc(ctx)
            }
            NavigateToValidationResult
        } catch (e: Exception) {
            Timber.e(e, "Error while submitting user consent")
            when (e) {
                is DccTicketingException -> e.errorMessage(serviceProvider = currentState.provider)
                else -> R.string.errors_generic_text_unknown_error_cause.toResolvingString()
            }.let { ShowErrorDialog(lazyErrorMessage = it) }
        }

        postEvent(event)
        currentIsLoading.compareAndSet(expect = true, update = false)
    }

    private fun postEvent(event: DccTicketingConsentTwoEvent) {
        Timber.d("postEvent(event=%s)", event)
        currentEvent.postValue(event)
    }

    data class UiState(
        val dccTicketingTransactionContext: DccTicketingTransactionContext,
        val certificate: CwaCovidCertificate
    ) {
        val testPartner get() = dccTicketingTransactionContext.allowlist?.first()?.serviceProvider
        val provider get() = dccTicketingTransactionContext.initializationData.serviceProvider
        val certificateItem get() = getCardItem(certificate)

        private fun getCardItem(certificate: CwaCovidCertificate): DccTicketingCertificateItem = when (certificate) {
            is TestCertificate -> DccTicketingTestCard.Item(certificate, false) {}
            is VaccinationCertificate -> DccTicketingVaccinationCard.Item(certificate, false) {}
            is RecoveryCertificate -> DccTicketingRecoveryCard.Item(certificate, false) {}
            else -> throw IllegalArgumentException("Certificate $certificate is not supported")
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingConsentTwoViewModel> {
        fun create(
            dccTicketingSharedViewModel: DccTicketingSharedViewModel,
            containerId: CertificateContainerId
        ): DccTicketingConsentTwoViewModel
    }
}
