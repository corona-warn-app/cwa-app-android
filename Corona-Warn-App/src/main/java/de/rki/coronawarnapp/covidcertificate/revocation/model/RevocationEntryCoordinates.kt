package de.rki.coronawarnapp.covidcertificate.revocation.model

import okio.ByteString

data class RevocationEntryCoordinates(
    /** The hex-encoded KID of a DCC (usually 16 characters) */
    val kid: ByteString,

    /** See [RevocationHashType] */
    val type: RevocationHashType,

    /** A hex-encoded byte representing the first byte of the hash */
    val x: ByteString,

    /** A hex-encoded byte representing the second byte of the hash */
    val y: ByteString
)
