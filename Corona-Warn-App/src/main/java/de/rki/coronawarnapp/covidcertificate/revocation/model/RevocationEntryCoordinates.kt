package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName

data class RevocationEntryCoordinates(
    /** The hex-encoded KID of a DCC (usually 16 characters) */
    @SerializedName("kid") val kid: String,

    /** See [RevocationHashType] */
    @SerializedName("type") val type: RevocationHashType,

    /** A hex-encoded byte representing the first byte of the hash */
    @SerializedName("x") val x: String,

    /** A hex-encoded byte representing the second byte of the hash */
    @SerializedName("y") val y: String
)
