package de.rki.coronawarnapp.dccticketing.core.allowlist.filtering

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK

data class DccJwkFilteringResult(
    val filteredAllowlist: Set<DccTicketingValidationServiceAllowListEntry>,
    val filteredJwkSet: Set<DccJWK>,
)
