package de.rki.coronawarnapp.covidcertificate.signature.core

import okio.ByteString

data class DscData(
    val dscList: List<Pair<ByteString, ByteString>>
)
