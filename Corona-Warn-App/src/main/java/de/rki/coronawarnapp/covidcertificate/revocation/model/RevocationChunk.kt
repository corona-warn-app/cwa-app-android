package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.fasterxml.jackson.annotation.JsonProperty
import okio.ByteString

data class RevocationChunk(
    @JsonProperty("hashes") val hashes: List<ByteString>
)

data class CachedRevocationChunk(
    /**
     * Used as an identifier of the respective hashes list
     */
    @JsonProperty("coordinates") val coordinates: RevocationEntryCoordinates,

    @JsonProperty("revocationChunk") val revocationChunk: RevocationChunk
)
