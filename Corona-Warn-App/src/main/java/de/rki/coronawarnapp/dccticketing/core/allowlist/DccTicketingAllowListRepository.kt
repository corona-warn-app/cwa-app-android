package de.rki.coronawarnapp.dccticketing.core.allowlist

import de.rki.coronawarnapp.dccticketing.core.allowlist.api.DccTicketingAllowListServer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccTicketingAllowListRepository @Inject constructor(
    private val dccTicketingAllowListServer: DccTicketingAllowListServer
) {
    // TODO
    val validationServiceAllowList: Flow<Set<DccTicketingValidationServiceAllowListEntry>> = flowOf(emptySet())
    val serviceProviderAllowListEntry: Flow<Set<DccTicketingServiceProviderAllowListEntry>> = flowOf(emptySet())
}
