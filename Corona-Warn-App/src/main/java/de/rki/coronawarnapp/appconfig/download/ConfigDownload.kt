package de.rki.coronawarnapp.appconfig.download

import com.google.gson.annotations.SerializedName
import org.joda.time.Duration
import org.joda.time.Instant

data class ConfigDownload(
    @SerializedName("rawData") val rawData: ByteArray,
    @SerializedName("serverTime") val serverTime: Instant,
    @SerializedName("localOffset") val localOffset: Duration
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigDownload

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
