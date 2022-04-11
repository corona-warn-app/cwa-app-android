package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName

data class RevocationKidList(
    @SerializedName("items") val items: List<RevocationKidListItem>
)

data class RevocationKidListItem(
    @SerializedName("kid") val kid: String,
    @SerializedName("hashTypes") val hashTypes: List<String>
)
