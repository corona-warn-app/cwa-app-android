package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingService(
    /** Identifier of the service identity document */
    @JsonProperty("id") val id: String,

    /** Type of the verification method */
    @JsonProperty("type") val type: String,

    /** URL to the service endpoint */
    @JsonProperty("serviceEndpoint") val serviceEndpoint: String,

    /** Name of the service */
    @JsonProperty("name") val name: String,
) : Parcelable
