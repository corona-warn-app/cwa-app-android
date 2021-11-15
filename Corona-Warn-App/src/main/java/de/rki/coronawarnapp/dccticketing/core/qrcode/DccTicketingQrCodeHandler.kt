package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccTicketingJwkFilter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    private val dccTicketingJwkFilter: DccTicketingJwkFilter,
) {

    suspend fun handleQrCode(qrCode: DccTicketingQrCode) {
        val transactionContext = DccTicketingTransactionContext(initializationData = qrCode.data)
        // todo Request Service Identity Document of Validation Decorator
        // todo Check against allowlist
        val filteringResult = dccTicketingJwkFilter.filter(emptySet())
        if (filteringResult.filteredJwkSet.isEmpty()) {
            throw DccTicketingAllowListException(ErrorCode.ALLOWLIST_NO_MATCH)
        }
        // todo return proper data when allow list pass
    }
}
