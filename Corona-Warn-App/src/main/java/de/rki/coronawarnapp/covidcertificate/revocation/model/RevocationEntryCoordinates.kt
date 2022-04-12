package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName
import okio.ByteString

data class RevocationEntryCoordinates(
    /** The hex-encoded KID of a DCC (usually 16 characters) */
    @SerializedName("kid")
    val kid: ByteString,

    /** See [RevocationHashType] */
    @SerializedName("type")
    val type: RevocationHashType,

    /** A hex-encoded byte representing the first byte of the hash */
    @SerializedName("x")
    val x: ByteString,

    /** A hex-encoded byte representing the second byte of the hash */
    @SerializedName("y")
    val y: ByteString
)
