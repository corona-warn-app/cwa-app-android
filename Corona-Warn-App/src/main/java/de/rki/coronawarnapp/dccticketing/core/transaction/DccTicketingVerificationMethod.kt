package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingVerificationMethod(
    /** Identifier of the service identity document */
    @SerializedName("id") val id: String,

    /** Type of the verification method */
    @SerializedName("type") val type: String,

    /** Controller of the verification method */
    @SerializedName("controller") val controller: String,

    /** A [DccJWK] */
    @SerializedName("publicKeyJwk") val publicKeyJwk: DccJWK? = null,

    /** An array of strings referencing id attributes of other verification methods */
    @SerializedName("verificationMethods") val verificationMethods: List<String>? = null,
) : Parcelable
