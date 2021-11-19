package de.rki.coronawarnapp.dccticketing.ui.consent.one

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class DccTicketingConsentOneViewModel @AssistedInject constructor(
    @Assisted private val dccTicketingTransactionContext: DccTicketingTransactionContext,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val showCloseDialog = SingleLiveEvent<Unit>()

    val uiState = MutableStateFlow(UiState(dccTicketingTransactionContext)).asLiveData()

    fun goBack() {
        showCloseDialog.postValue(Unit)
    }

    data class UiState(
        val dccTicketingTransactionContext: DccTicketingTransactionContext
    ) {
        val provider get() = dccTicketingTransactionContext.initializationData.serviceProvider
        val subject get() = dccTicketingTransactionContext.initializationData.subject
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingConsentOneViewModel> {
        fun create(
            dccTicketingTransactionContext: DccTicketingTransactionContext
        ): DccTicketingConsentOneViewModel
    }
}
