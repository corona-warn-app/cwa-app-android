package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccTicketingJwkFilter
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    private val requestService: DccTicketingRequestService,
    private val dccTicketingJwkFilter: DccTicketingJwkFilter,
    private val allowListRepository: DccTicketingAllowListRepository,
) {
    suspend fun handleQrCode(qrCode: DccTicketingQrCode): DccTicketingTransactionContext {
        val validationServiceAllowList = allowListRepository.refresh().validationServiceAllowList
        val transactionContext = DccTicketingTransactionContext(
            initializationData = qrCode.data
        ).decorate(validationServiceAllowList)

        val filteringResult = dccTicketingJwkFilter.filter(
            transactionContext.validationServiceJwkSet.orEmpty(),
            validationServiceAllowList
        )
        if (filteringResult.filteredJwkSet.isEmpty()) {
            throw DccTicketingAllowListException(ErrorCode.ALLOWLIST_NO_MATCH)
        }

        return transactionContext.copy(
            allowlist = filteringResult.filteredAllowlist,
            validationServiceJwkSet = filteringResult.filteredJwkSet
        )
    }

    suspend fun DccTicketingTransactionContext.decorate(
        validationServiceAllowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): DccTicketingTransactionContext {
        val decorator =
            requestService.requestValidationDecorator(
                initializationData.serviceIdentity,
                validationServiceAllowList
            )
        return copy(
            accessTokenService = decorator.accessTokenService,
            accessTokenServiceJwkSet = decorator.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = decorator.accessTokenSignJwkSet,
            validationService = decorator.validationService,
            validationServiceJwkSet = decorator.validationServiceJwkSet,
        )
    }
}
