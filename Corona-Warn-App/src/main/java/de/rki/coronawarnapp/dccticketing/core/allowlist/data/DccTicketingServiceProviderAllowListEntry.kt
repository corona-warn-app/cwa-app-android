package de.rki.coronawarnapp.dccticketing.core.allowlist.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okio.ByteString

@Parcelize
data class DccTicketingServiceProviderAllowListEntry(
    /** The SHA-256 of the service identity endpoint of the Validation Decorator */
    val serviceIdentityHash: ByteString
) : Parcelable
