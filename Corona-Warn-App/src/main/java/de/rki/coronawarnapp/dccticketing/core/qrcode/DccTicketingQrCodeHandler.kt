package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    private val requestService: DccTicketingRequestService
) {

    suspend fun handleQrCode(qrCode: DccTicketingQrCode) {
        var transactionContext = DccTicketingTransactionContext(
            initializationData = qrCode.data
        ).decorate()

        // todo Check against allowlist
    }

    suspend fun DccTicketingTransactionContext.decorate(): DccTicketingTransactionContext {
        val decorator = requestService.requestValidationDecorator(initializationData.serviceIdentity)
        return copy(
            accessTokenService = decorator.accessTokenService,
            accessTokenServiceJwkSet = decorator.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = decorator.accessTokenSignJwkSet,
            validationService = decorator.validationService,
            validationServiceJwkSet = decorator.validationServiceJwkSet,
        )
    }
}
