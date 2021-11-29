package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext

val dccTicketingTransactionContextPassed = DccTicketingTransactionContext(
    initializationData = DccTicketingQrCodeData(
        protocol = "",
        protocolVersion = "",
        serviceIdentity = "",
        privacyUrl = "",
        token = "",
        consent = "",
        subject = "",
        serviceProvider = "Anbietername"
    ),
    resultTokenPayload = DccTicketingResultToken(
        iss = "",
        iat = 1622041200,
        exp = 0,
        sub = "",
        category = emptyList(),
        result = DccTicketingResultToken.DccResult.PASS,
        confirmation = "",
        results = emptyList()
    )
)

val dccTicketingTransactionContextOpen = DccTicketingTransactionContext(
    initializationData = DccTicketingQrCodeData(
        protocol = "",
        protocolVersion = "",
        serviceIdentity = "",
        privacyUrl = "",
        token = "",
        consent = "",
        subject = "",
        serviceProvider = "Anbietername"
    ),
    resultTokenPayload = DccTicketingResultToken(
        iss = "",
        iat = 1622041200,
        exp = 0,
        sub = "",
        category = listOf(),
        result = DccTicketingResultToken.DccResult.OPEN,
        confirmation = "",
        results = listOf(
            DccTicketingResultItem(identifier = "TR-002", result = DccTicketingResultToken.DccResult.OPEN, type = "TYPE", details = "Ein Antigentest ist maximal 48h gültig.")
        )
    )
)
val dccTicketingTransactionContextFailed = DccTicketingTransactionContext(
    initializationData = DccTicketingQrCodeData(
        protocol = "",
        protocolVersion = "",
        serviceIdentity = "",
        privacyUrl = "",
        token = "",
        consent = "",
        subject = "",
        serviceProvider = "Anbietername"
    ),
    resultTokenPayload = DccTicketingResultToken(
        iss = "",
        iat = 1622041200,
        exp = 0,
        sub = "",
        category = listOf(),
        result = DccTicketingResultToken.DccResult.FAIL,
        confirmation = "",
        results = listOf(
            DccTicketingResultItem(identifier = "TR-002", result = DccTicketingResultToken.DccResult.FAIL, type = "TYPE", details = "Ein Testzertifikat muss von einem zertifizierten Testzentrum ausgestellt werden.")
        )
    )
)
