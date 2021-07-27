package de.rki.coronawarnapp.covidcertificate.signature.core

import okio.ByteString
import org.joda.time.Instant

data class DscData(
    val dscList: List<DscItem>,
    val updatedAt: Instant
)

data class DscItem(
    val kid: ByteString,
    val data: ByteString
)
