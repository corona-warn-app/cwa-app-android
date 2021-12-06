package de.rki.coronawarnapp.dccticketing.core.allowlist.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import okio.ByteString

@Parcelize
data class DccTicketingValidationServiceAllowListEntry(
    /** A display name for the provider of the Validation Service */
    @SerializedName("serviceProvider") val serviceProvider: String,

    /** The hostname of the Validation Service */
    @SerializedName("hostname") val hostname: String,

    /** The SHA-256 fingerprint of the certificate of the Validation Service */
    @SerializedName("fingerprint256") val fingerprint256: ByteString
) : Parcelable
