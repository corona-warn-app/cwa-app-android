package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Suppress("ConstructorParameterNaming")
@Parcelize
data class DccTicketingServiceIdentityDocument(
    /** Identifier of the service identity document */
    @JsonProperty("id") val id: String,

    /** An array of [DccTicketingVerificationMethod] objects */
    @JsonProperty("verificationMethod") val verificationMethod: List<DccTicketingVerificationMethod>,

    @JsonProperty("service") val _service: List<DccTicketingService>? = null
) : Parcelable {
    /** An array of [DccTicketingService] objects */
    @get:JsonIgnore
    val service: List<DccTicketingService>
        get() = _service.orEmpty()
}
