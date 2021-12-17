package de.rki.coronawarnapp.dccticketing.ui.shared

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * Shares the [DccTicketingTransactionContext] between all DccTicketing destinations.
 */
class DccTicketingSharedViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    private val currentTransactionContext = MutableStateFlow(savedTransactionContext)

    val transactionContext: Flow<DccTicketingTransactionContext> = currentTransactionContext.filterNotNull()

    init {
        transactionContext
            .onEach { savedTransactionContext = it }
            .catch { Timber.tag(TAG).e(it, "Failed to save DccTicketingTransactionContext") }
            .launchIn(viewModelScope)
    }

    fun updateTransactionContext(ctx: DccTicketingTransactionContext) {
        Timber.tag(TAG).d("updateTransactionContext(ctx=%s)", ctx)
        currentTransactionContext.tryEmit(ctx).also {
            Timber.tag(TAG).d("Update was successful? %s", it)
        }
    }

    private var savedTransactionContext: DccTicketingTransactionContext?
        get() = savedState[TRANSACTION_CONTEXT_SAVED_STATE_KEY]
        set(value) {
            Timber.tag(TAG).v("Saving %s into savedStateHandle", value)
            savedState[TRANSACTION_CONTEXT_SAVED_STATE_KEY] = value
        }

    companion object {
        private val TAG = tag<DccTicketingSharedViewModel>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val TRANSACTION_CONTEXT_SAVED_STATE_KEY = "transaction_context_saved_state_key"
    }
}
