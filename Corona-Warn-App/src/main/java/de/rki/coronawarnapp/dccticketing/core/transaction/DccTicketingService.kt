package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingService(
    /** Identifier of the service identity document */
    @SerializedName("id") val id: String,

    /** Type of the verification method */
    @SerializedName("type") val type: String,

    /** URL to the service endpoint */
    @SerializedName("serviceEndpoint") val serviceEndpoint: String,

    /** Name of the service */
    @SerializedName("name") val name: String,
) : Parcelable
