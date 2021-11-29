package de.rki.coronawarnapp.dccticketing.ui.shared

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

/**
 * Shares the [DccTicketingTransactionContext] between all DccTicketing destinations.
 */
class DccTicketingSharedViewModel : ViewModel() {

    private val currentTransactionContext = MutableStateFlow<DccTicketingTransactionContext?>(null)

    val transactionContext: Flow<DccTicketingTransactionContext> = currentTransactionContext.filterNotNull()

    fun updateTransactionContext(ctx: DccTicketingTransactionContext) {
        Timber.d("updateTransactionContext(ctx=%s)", ctx)
        currentTransactionContext.tryEmit(ctx).also { Timber.d("Update was successful? %s", it) }
    }
}
