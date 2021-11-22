package de.rki.coronawarnapp.dccticketing.core.allowlist.api

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListEntry
import de.rki.coronawarnapp.util.security.SignatureValidation
import javax.inject.Inject

@Reusable
class DccTicketingAllowListServer @Inject constructor(
    private val signatureValidation: SignatureValidation,
    // TODO private val allowListApi1: Lazy<DccTicketingAllowListApi1>
) {
    suspend fun getAllowList(): Set<DccTicketingAllowListEntry> {
        // TODO
        return emptySet()
    }
}
