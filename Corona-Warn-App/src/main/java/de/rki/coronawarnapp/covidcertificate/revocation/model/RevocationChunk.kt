package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.google.gson.annotations.SerializedName
import okio.ByteString

data class RevocationChunk(
    @SerializedName("hashes") val hashes: List<ByteString>
)

data class CachedRevocationChunk(
    /**
     * Used as an identifier of the respective hash list
     */
    @SerializedName("coordinates")
    val coordinates: RevocationEntryCoordinates,

    @SerializedName("revocationChunk")
    val revocationChunk: RevocationChunk
)
