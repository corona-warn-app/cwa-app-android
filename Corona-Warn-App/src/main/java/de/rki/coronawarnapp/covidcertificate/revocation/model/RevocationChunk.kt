package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName
import okio.ByteString

data class RevocationChunk(
    @SerializedName("hashes") val hashes: List<ByteString>
)

data class CachedRevocationChunk(
    @SerializedName("kid") val kid: String,
    @SerializedName("hashType") val hashType: RevocationHashType,
    @SerializedName("x") val x: String,
    @SerializedName("y") val y: String,
    @SerializedName("revocationChunk") val revocationChunk: RevocationChunk
)
