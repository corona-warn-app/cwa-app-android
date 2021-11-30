package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext

val initializationData = DccTicketingQrCodeData(
    protocol = "",
    protocolVersion = "",
    serviceIdentity = "",
    privacyUrl = "",
    token = "",
    consent = "",
    subject = "",
    serviceProvider = "Anbietername"
)
val resultTokenPayload = DccTicketingResultToken(
    iss = "",
    iat = 1622041200,
    exp = 0,
    sub = "",
    category = emptyList(),
    confirmation = "",
    result = DccTicketingResultToken.DccResult.PASS,
    results = emptyList()
)
val dccTicketingTransactionContextPassed = DccTicketingTransactionContext(
    initializationData = initializationData,
    resultTokenPayload = resultTokenPayload
)

val dccTicketingTransactionContextOpen = dccTicketingTransactionContextPassed.copy(
    resultTokenPayload = resultTokenPayload.copy(
        result = DccTicketingResultToken.DccResult.OPEN,
        results = listOf(
            DccTicketingResultItem(
                identifier = "TR-002",
                result = DccTicketingResultToken.DccResult.OPEN,
                type = "TYPE",
                details = "Ein Antigentest ist maximal 48h gültig."
            )
        )
    )
)
val dccTicketingTransactionContextFailed = dccTicketingTransactionContextPassed.copy(
    resultTokenPayload = resultTokenPayload.copy(
        result = DccTicketingResultToken.DccResult.FAIL,
        results = listOf(
            DccTicketingResultItem(
                identifier = "TR-002",
                result = DccTicketingResultToken.DccResult.FAIL,
                type = "TYPE",
                details = "Ein Testzertifikat muss von einem zertifizierten Testzentrum ausgestellt werden."
            )
        )
    )
)
