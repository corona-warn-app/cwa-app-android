package de.rki.coronawarnapp.covidcertificate.signature.core

import okio.ByteString

data class DscData(
    val dscList: List<DscItem>
)

data class DscItem(
    val kid: String,
    val data: ByteString
)
