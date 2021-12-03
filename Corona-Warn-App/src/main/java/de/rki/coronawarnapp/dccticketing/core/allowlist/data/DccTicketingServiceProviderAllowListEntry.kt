package de.rki.coronawarnapp.dccticketing.core.allowlist.data

import okio.ByteString

data class DccTicketingServiceProviderAllowListEntry(
    /** The SHA-256 of the service identity endpoint of the Validation Decorator */
    val serviceIdentityHash: ByteString
)
