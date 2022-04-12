package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName
import okio.ByteString

data class RevocationChunk(
    @SerializedName("hashes") val hashes: List<ByteString>
)

data class CachedKidTypeXYChunk(
    @SerializedName("kid") val kid: ByteString,
    @SerializedName("hashType") val hashType: RevocationHashType,
    @SerializedName("x") val x: ByteString,
    @SerializedName("y") val y: ByteString,
    @SerializedName("revocationChunk") val revocationChunk: RevocationChunk
)
