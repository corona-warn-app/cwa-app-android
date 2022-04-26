package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName
import okio.ByteString

data class RevocationKidTypeIndex(
    @SerializedName("items") val items: List<RevocationKidTypeIndexItem>
)

data class RevocationKidTypeIndexItem(
    @SerializedName("x") val x: ByteString,
    @SerializedName("y") val y: List<ByteString>
)

data class CachedRevocationKidTypeIndex(
    @SerializedName("kid") val kid: ByteString,
    @SerializedName("hashType") val hashType: RevocationHashType,
    @SerializedName("revocationKidTypeIndex") val revocationKidTypeIndex: RevocationKidTypeIndex
)
