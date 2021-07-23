package de.rki.coronawarnapp.covidcertificate.signature.core

import okio.ByteString
import org.joda.time.Instant

data class DscData(
    val dscList: List<Pair<ByteString, ByteString>>,
    val updatedAt: Instant,
)
