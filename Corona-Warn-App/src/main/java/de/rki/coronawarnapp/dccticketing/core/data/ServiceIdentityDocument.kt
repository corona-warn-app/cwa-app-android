package de.rki.coronawarnapp.dccticketing.core.data

import com.google.gson.annotations.SerializedName

data class ServiceIdentityDocument(
    /** Identifier of the service identity document */
    @SerializedName("id") val id: String,

    /** An array of Verification Method objects */
    @SerializedName("verificationMethod") val verificationMethod: List<VerificationMethod>,

    /** An array of Service objects */
    @SerializedName("service") val service: List<Service>? = null
) {

    data class VerificationMethod(
        /** Identifier of the service identity document */
        @SerializedName("id") val id: String,

        /** Type of the verification method */
        @SerializedName("type") val type: String,

        /** Controller of the verification method */
        @SerializedName("controller") val controller: String,

        /** A JWK [TicketValidationJsonWebKey] */
        @SerializedName("publicKeyJwk") val publicKeyJwk: TicketValidationJsonWebKey? = null,

        /** An array of strings referencing id attributes of other verification methods */
        @SerializedName("verificationMethods") val verificationMethods: List<String>? = null,
    )

    data class Service(
        /** Identifier of the service identity document */
        @SerializedName("id") val id: String,

        /** Type of the verification method */
        @SerializedName("type") val type: String,

        /** URL to the service endpoint */
        @SerializedName("serviceEndpoint") val serviceEndpoint: String,

        /** Name of the service */
        @SerializedName("name") val name: String,
    )
}
