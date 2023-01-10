package de.rki.coronawarnapp.appconfig.internal

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.Instant

data class InternalConfigData(
    @JsonProperty("rawData") val rawData: ByteArray,
    @JsonProperty("etag") val etag: String,
    @JsonProperty("serverTime") val serverTime: Instant,
    @JsonProperty("localOffset") val localOffset: Duration,
    @JsonProperty("cacheValidity") val cacheValidity: Duration
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InternalConfigData

        if (!rawData.contentEquals(other.rawData)) return false
        if (serverTime != other.serverTime) return false
        if (localOffset != other.localOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawData.contentHashCode()
        result = 31 * result + serverTime.hashCode()
        result = 31 * result + localOffset.hashCode()
        return result
    }
}
