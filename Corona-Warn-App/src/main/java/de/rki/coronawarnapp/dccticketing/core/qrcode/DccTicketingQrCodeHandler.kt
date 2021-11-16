package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceDecorator
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    val decorator: DccTicketingServiceDecorator
) {

    suspend fun handleQrCode(qrCode: DccTicketingQrCode) {
        var transactionContext = DccTicketingTransactionContext(
            initializationData = qrCode.data
        ).decorate()

        // todo Check against allowlist
    }

    suspend fun DccTicketingTransactionContext.decorate(): DccTicketingTransactionContext {
        val decoration = decorator.decorate(initializationData.serviceIdentity)
        return copy(
            accessTokenService = decoration.tokenService.accessTokenService,
            accessTokenServiceJwkSet = decoration.tokenService.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = decoration.tokenService.accessTokenSignJwkSet,
            validationService = decoration.validationService.validationService,
            validationServiceJwkSet = decoration.validationService.validationServiceJwkSet,
        )
    }
}
