package de.rki.coronawarnapp.dccticketing.core.allowlist.data

data class DccTicketingAllowListContainer(
    val serviceProviderAllowList: Set<DccTicketingServiceProviderAllowListEntry> = emptySet(),
    val validationServiceAllowList: Set<DccTicketingValidationServiceAllowListEntry> = emptySet()
)
