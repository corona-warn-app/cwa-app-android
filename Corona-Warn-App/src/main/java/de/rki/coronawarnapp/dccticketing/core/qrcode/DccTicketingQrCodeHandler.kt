package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccTicketingJwkFilter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    private val requestService: DccTicketingRequestService
) {
class DccTicketingQrCodeHandler @Inject constructor(
    private val dccTicketingJwkFilter: DccTicketingJwkFilter,
) {

    suspend fun handleQrCode(qrCode: DccTicketingQrCode) {
        var transactionContext = DccTicketingTransactionContext(
            initializationData = qrCode.data
        ).decorate()

        // todo Check against allowlist
        val filteringResult = dccTicketingJwkFilter.filter(emptySet())
        if (filteringResult.filteredJwkSet.isEmpty()) {
            throw DccTicketingAllowListException(ErrorCode.ALLOWLIST_NO_MATCH)
        }
        // todo return proper data when allow list pass
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
