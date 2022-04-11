package de.rki.coronawarnapp.covidcertificate.revocation.model

import okio.ByteString

data class RevocationEntryCoordinates(
    val kid: ByteString,
    val type: RevocationHashType,
    val x: ByteString,
    val y: ByteString
)
