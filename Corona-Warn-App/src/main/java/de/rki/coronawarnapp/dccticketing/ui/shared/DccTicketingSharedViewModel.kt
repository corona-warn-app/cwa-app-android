package de.rki.coronawarnapp.dccticketing.ui.shared

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

/**
 * Shares the [DccTicketingTransactionContext] between all DccTicketing destinations.
 */
class DccTicketingSharedViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    private val currentTransactionContext = MutableStateFlow(
        getSavedTransactionContext()
    )

    val transactionContext: Flow<DccTicketingTransactionContext> = currentTransactionContext.filterNotNull()

    fun updateTransactionContext(ctx: DccTicketingTransactionContext) {
        Timber.d("updateTransactionContext(ctx=%s)", ctx)
        currentTransactionContext.tryEmit(ctx).also {
            Timber.d("Update was successful? %s", it)
            saveTransactionContext(ctx)
        }
    }

    // returns the transaction context that is stored in the savedStateHandle, or null otherwise
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getSavedTransactionContext(): DccTicketingTransactionContext? {
        val transactionContext: DccTicketingTransactionContext? = savedState.get(TRANSACTION_CONTEXT_SAVED_STATE_KEY)
        Timber.d("DccTicketingTransactionContext: %s loaded from savedStateHandle", transactionContext)
        return transactionContext
    }

    private fun saveTransactionContext(transactionContext: DccTicketingTransactionContext) {
        Timber.d("Saving DccTicketingTransactionContext: %s into savedStateHandle", transactionContext)
        savedState.set(TRANSACTION_CONTEXT_SAVED_STATE_KEY, transactionContext)
    }

    companion object {
        private const val TRANSACTION_CONTEXT_SAVED_STATE_KEY = "transaction_context_saved_state_key"
    }
}
