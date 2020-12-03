package de.rki.coronawarnapp.appconfig.internal

import com.google.gson.annotations.SerializedName
import org.joda.time.Duration
import org.joda.time.Instant

data class InternalConfigData(
    @SerializedName("rawData") val rawData: ByteArray,
    @SerializedName("etag") val etag: String,
    @SerializedName("serverTime") val serverTime: Instant,
    @SerializedName("localOffset") val localOffset: Duration,
    @SerializedName("cacheValidity") val cacheValidity: Duration
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
