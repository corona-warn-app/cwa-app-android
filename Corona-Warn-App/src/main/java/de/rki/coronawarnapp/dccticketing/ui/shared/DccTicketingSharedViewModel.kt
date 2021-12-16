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

    private val currentTransactionContext = MutableStateFlow<DccTicketingTransactionContext?>(
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getSavedTransactionContext(): DccTicketingTransactionContext? =
        savedState.get(TRANSACTION_CONTEXT_SAVED_STATE_KEY)

    private fun saveTransactionContext(ctx: DccTicketingTransactionContext) {
        savedState.set(TRANSACTION_CONTEXT_SAVED_STATE_KEY, ctx)
    }

    companion object {
        private const val TRANSACTION_CONTEXT_SAVED_STATE_KEY = "transaction_context_saved_state_key"
    }
}
