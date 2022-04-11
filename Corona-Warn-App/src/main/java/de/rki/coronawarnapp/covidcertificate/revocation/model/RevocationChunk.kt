package de.rki.coronawarnapp.covidcertificate.revocation.model

import okio.ByteString

data class RevocationChunk(
    val hashes: List<ByteString>
)
