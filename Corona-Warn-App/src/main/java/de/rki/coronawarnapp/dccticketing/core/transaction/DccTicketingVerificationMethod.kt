package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingVerificationMethod(
    /** Identifier of the service identity document */
    @JsonProperty("id") val id: String,

    /** Type of the verification method */
    @JsonProperty("type") val type: String,

    /** Controller of the verification method */
    @JsonProperty("controller") val controller: String,

    /** A [DccJWK] */
    @JsonProperty("publicKeyJwk") val publicKeyJwk: DccJWK? = null,

    /** An array of strings referencing id attributes of other verification methods */
    @JsonProperty("verificationMethods") val verificationMethods: List<String>? = null,
) : Parcelable
