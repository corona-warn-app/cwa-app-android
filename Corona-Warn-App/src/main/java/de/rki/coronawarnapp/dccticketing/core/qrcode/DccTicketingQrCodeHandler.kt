package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor() {

    suspend fun handleQrCode(qrCode: DccTicketingQrCode) {
        val transactionContext = DccTicketingTransactionContext(initializationData = qrCode.data)
        // todo Request Service Identity Document of Validation Decorator
        // todo Check against allowlist
    }
}
