package de.rki.coronawarnapp.dccticketing.core.service

import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingSubmissionService @Inject constructor() {
    suspend fun startDccSubmission(
        transactionContext: DccTicketingTransactionContext,
        dccRawQrCode: QrCodeString
    ): DccTicketingResultToken {
        // TODO
        throw NotImplementedError()
    }
}
