package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.fasterxml.jackson.annotation.JsonProperty
import okio.ByteString

data class RevocationEntryCoordinates(
    /** The hex-encoded KID of a DCC (usually 16 characters) */
    @JsonProperty("kid") val kid: ByteString,

    /** See [RevocationHashType] */
    @JsonProperty("type") val type: RevocationHashType,

    /** A hex-encoded byte representing the first byte of the hash */
    @JsonProperty("x") val x: ByteString,

    /** A hex-encoded byte representing the second byte of the hash */
    @JsonProperty("y") val y: ByteString
)
