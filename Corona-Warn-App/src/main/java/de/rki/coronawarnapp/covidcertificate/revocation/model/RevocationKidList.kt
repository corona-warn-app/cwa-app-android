package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName
import okio.ByteString

data class RevocationKidList(
    @SerializedName("items") val items: List<RevocationKidListItem>
)

data class RevocationKidListItem(
    @SerializedName("kid") val kid: ByteString,
    @SerializedName("hashTypes") val hashTypes: List<RevocationHashType>
)
