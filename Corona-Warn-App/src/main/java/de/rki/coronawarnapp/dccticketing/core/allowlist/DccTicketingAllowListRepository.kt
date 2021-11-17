package de.rki.coronawarnapp.dccticketing.core.allowlist

import de.rki.coronawarnapp.dccticketing.core.allowlist.api.DccTicketingAllowListServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccTicketingAllowListRepository @Inject constructor(
    private val dccTicketingAllowListServer: DccTicketingAllowListServer
) {
    // TODO
    val allowList: Flow<Set<DccTicketingAllowListEntry>> = flowOf(setOf())
}
