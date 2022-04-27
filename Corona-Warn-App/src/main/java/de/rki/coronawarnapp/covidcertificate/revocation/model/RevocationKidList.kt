package de.rki.coronawarnapp.covidcertificate.revocation.model

import okio.ByteString

data class RevocationKidList(
    val items: Set<RevocationKidListItem>
)

data class RevocationKidListItem(
    val kid: ByteString,
    val hashTypes: Set<RevocationHashType>
)
