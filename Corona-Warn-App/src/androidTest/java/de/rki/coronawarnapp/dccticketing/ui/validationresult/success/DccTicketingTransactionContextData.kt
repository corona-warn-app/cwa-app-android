package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
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
    ),
    resultTokenPayload = DccTicketingResultToken(
        iss = "",
        iat = 1,
        exp = 0,
        sub = "",
        category = listOf(),
        result = "CHK",
        confirmation = "",
        results = listOf(
            DccTicketingResultItem(identifier = "ID", result = "NOK", type = "TYPE", details = "Description")
        )
    )
)
