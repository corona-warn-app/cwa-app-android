package de.rki.coronawarnapp.dccticketing.ui.consent.one

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DccTicketingConsentOneViewModel @AssistedInject constructor(
    @Assisted private val dccTicketingSharedViewModel: DccTicketingSharedViewModel,
    @Assisted private val qrcodeSharedViewModel: QrcodeSharedViewModel,
    @Assisted private val transactionContextIdentifier: String,
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingConsentOneProcessor: DccTicketingConsentOneProcessor
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        val ctx = qrcodeSharedViewModel.dccTicketingTransactionContext(transactionContextIdentifier)
        dccTicketingSharedViewModel.initializeTransactionContext(ctx)
    }

    private val currentIsLoading = MutableStateFlow(false)
    val isLoading: LiveData<Boolean> = currentIsLoading.asLiveData()

    private val currentEvent = SingleLiveEvent<DccTicketingConsentOneEvent>()
    val events: LiveData<DccTicketingConsentOneEvent> = currentEvent

    private val currentUiState = dccTicketingSharedViewModel.transactionContext
        .map { UiState(it) }
    val uiState: LiveData<UiState> = currentUiState.asLiveData2()

    fun onUserCancel() {
        Timber.d("onUserCancel()")
        postEvent(ShowCancelConfirmationDialog)
    }

    fun goBack() = postEvent(NavigateBack)

    fun showPrivacyInformation() = postEvent(NavigateToPrivacyInformation)

    fun onUserConsent() = launch {
        Timber.d("onUserConsent()")
        currentIsLoading.compareAndSet(expect = false, update = true)
        val event = try {
            dccTicketingSharedViewModel.updateTransactionContext { ctx ->
                dccTicketingConsentOneProcessor.updateTransactionContext(ctx = ctx)
            }
            NavigateToCertificateSelection
        } catch (e: Exception) {
            Timber.e(e, "Error while processing user consent")
            val lazyErrorMessage = when (e) {
                is DccTicketingException -> {
                    val serviceProvider = currentUiState.first().provider
                    e.errorMessage(serviceProvider = serviceProvider)
                }
                else -> R.string.errors_generic_text_unknown_error_cause.toResolvingString()
            }
            ShowErrorDialog(lazyErrorMessage = lazyErrorMessage)
        }
        postEvent(event = event)
        currentIsLoading.compareAndSet(expect = true, update = false)
    }

    private fun postEvent(event: DccTicketingConsentOneEvent) {
        Timber.d("postEvent(event=%s)", event)
        currentEvent.postValue(event)
    }

    data class UiState(
        private val dccTicketingTransactionContext: DccTicketingTransactionContext
    ) {
        val provider get() = dccTicketingTransactionContext.initializationData.serviceProvider
        val subject get() = dccTicketingTransactionContext.initializationData.subject
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingConsentOneViewModel> {
        fun create(
            dccTicketingSharedViewModel: DccTicketingSharedViewModel,
            qrcodeSharedViewModel: QrcodeSharedViewModel,
            transactionContextIdentifier: String
        ): DccTicketingConsentOneViewModel
    }
}
