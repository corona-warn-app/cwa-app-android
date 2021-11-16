package de.rki.coronawarnapp.dccticketing.core.allowlist.filtering

import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DccTicketingJwkFilter @Inject constructor(
    private val dccTicketingAllowListRepository: DccTicketingAllowListRepository
) {

    suspend fun filter(jwkSet: Set<DccJWK>): DccJwkFilteringResult {
        val allowList = dccTicketingAllowListRepository.allowList.first()
        // TODO
        return DccJwkFilteringResult(
            filteredAllowlist = emptySet(),
            filteredJwkSet = emptySet()
        )
    }
}
