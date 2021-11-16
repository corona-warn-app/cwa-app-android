package de.rki.coronawarnapp.dccticketing.core.transaction

import com.google.gson.annotations.SerializedName

data class DccTicketingVerificationMethod(
    @SerializedName("id")
    val id: String,    // Identifier of the service identity document
    @SerializedName("type")
    val type: String, // Type of the verification method
    @SerializedName("controller")
    val controller: String, // Controller of the verification method
    @SerializedName("publicKeyJwk")
    val publicKeyJwk: DccJWK?,    //(optional)	A JWK (see Data Structure of a JSON Web Key (JWK))
    @SerializedName("verificationMethods")
    val verificationMethods: List<String>?, //(optional)	An array of strings referencing id attributes of other verification methods. As this parameter is optional, it may be defaulted to an empty array.
)
