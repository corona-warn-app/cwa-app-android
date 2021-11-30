package de.rki.coronawarnapp.dccticketing.core.allowlist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okio.ByteString

@Parcelize
data class DccTicketingAllowListEntry(
    val serviceProvider: String, // A display name for the provider of the Validation Service
    val hostname: String, // The hostname of the Validation Service
    val fingerprint256: ByteString // The SHA-256 fingerprint of the certificate of the Validation Service
) : Parcelable
