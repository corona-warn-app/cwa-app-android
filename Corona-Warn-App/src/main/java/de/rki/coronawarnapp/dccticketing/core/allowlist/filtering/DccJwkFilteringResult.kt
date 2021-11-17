package de.rki.coronawarnapp.dccticketing.core.allowlist.filtering

import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK

data class DccJwkFilteringResult(
    val filteredAllowlist: Set<DccTicketingAllowListEntry>,
    val filteredJwkSet: Set<DccJWK>,
)
