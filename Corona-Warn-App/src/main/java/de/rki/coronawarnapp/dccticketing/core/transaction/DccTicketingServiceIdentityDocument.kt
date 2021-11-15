package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingServiceIdentityDocument(
    /** Identifier of the service identity document */
    @SerializedName("id") val id: String,

    /** An array of [DccTicketingVerificationMethod] objects */
    @SerializedName("verificationMethod") val verificationMethod: List<DccTicketingVerificationMethod>,

    /** An array of [DccTicketingService] objects */
    @SerializedName("service") private val _service: List<DccTicketingService>? = null
) : Parcelable {
    val service: List<DccTicketingService>
        get() = _service.orEmpty()
}
