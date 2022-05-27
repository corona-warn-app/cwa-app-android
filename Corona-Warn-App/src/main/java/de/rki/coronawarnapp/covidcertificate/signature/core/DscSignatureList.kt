package de.rki.coronawarnapp.covidcertificate.signature.core

import okio.ByteString
import java.time.Instant

data class DscSignatureList(
    val dscList: List<DscItem>,
    val updatedAt: Instant,
)

data class DscItem(
    val kid: String,
    val data: ByteString
)
