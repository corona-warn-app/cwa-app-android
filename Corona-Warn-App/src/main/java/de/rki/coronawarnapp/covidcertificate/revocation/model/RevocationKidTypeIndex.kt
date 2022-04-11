package de.rki.coronawarnapp.covidcertificate.revocation.model

import okio.ByteString

data class RevocationKidTypeIndex(
    val items: List<RevocationKidTypeIndexItem>
)

data class RevocationKidTypeIndexItem(
    val x: ByteString,
    val y: List<ByteString>
)
