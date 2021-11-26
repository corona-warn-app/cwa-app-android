package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext

val transactionContextSampleData = DccTicketingTransactionContext(
    initializationData = DccTicketingQrCodeData(
        protocol = "",
        protocolVersion = "",
        serviceIdentity = "",
        privacyUrl = "",
        token = "",
        consent = "",
        subject = "",
        serviceProvider = ""
    )
)
