package de.rki.coronawarnapp.dccticketing.core.data

import com.google.gson.annotations.SerializedName

data class TicketValidationJsonWebKey(
    /**
     * a base64-encoded string that represents an x509 certificate.
     *
     * The attribute typically needs to be parsed to a native x509 certificate object
     * to obtain certificate information such as the included public key.
     */
    @SerializedName("x5x") val x5x: String,

    /**
     * a base64-encoded string.
     *
     * The attribute can typically be treated as a regular string and there is no need to parse
     * this to a byte sequence ( Data / ByteArray ).
     */
    @SerializedName("kid") val kid: String,

    /**
     * a string describing the algorithm.
     *
     * The attribute typically has one of the values ES256 , RS256 , or PS256. However, the data structure should allow
     * for other values. Unsupported algorithms shall only be rejected by operations that process JWKs.
     */
    @SerializedName("alg") val alg: String,

    /** a string of either sig or enc that indicates the purpose of the JWK (signature or encryption) */
    @SerializedName("use") val use: String,
)
