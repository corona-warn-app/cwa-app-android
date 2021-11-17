package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    val decoratorRequestProcessor: ValidationDecoratorRequestProcessor
) {

    suspend fun handleQrCode(qrCode: DccTicketingQrCode) {
        var transactionContext = DccTicketingTransactionContext(
            initializationData = qrCode.data
        ).decorate()

        // todo Check against allowlist
    }

    suspend fun DccTicketingTransactionContext.decorate(): DccTicketingTransactionContext {
        val decoration = decoratorRequestProcessor.requestValidationDecorator(initializationData.serviceIdentity)
        return copy(
            accessTokenService = decoration.accessTokenService,
            accessTokenServiceJwkSet = decoration.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = decoration.accessTokenSignJwkSet,
            validationService = decoration.validationService,
            validationServiceJwkSet = decoration.validationServiceJwkSet,
        )
    }
}
