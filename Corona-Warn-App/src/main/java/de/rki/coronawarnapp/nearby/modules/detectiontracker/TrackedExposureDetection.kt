package de.rki.coronawarnapp.nearby.modules.detectiontracker

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

@Keep
data class TrackedExposureDetection(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("startedAt") val startedAt: Instant,
    @SerializedName("result") val result: Result? = null,
    @SerializedName("finishedAt") val finishedAt: Instant? = null,
    @SerializedName("enfVersion") val enfVersion: EnfVersion? = null
) {

    val isCalculating: Boolean
        get() = finishedAt == null
    val isSuccessful: Boolean
        get() = (result == Result.NO_MATCHES || result == Result.UPDATED_STATE)

    @Keep
    enum class Result {
        @SerializedName("NO_MATCHES")
        NO_MATCHES,

        @SerializedName("UPDATED_STATE")
        UPDATED_STATE,

        @SerializedName("TIMEOUT")
        TIMEOUT
    }

    enum class EnfVersion {
        @SerializedName("V1_LEGACY_MODE")
        V1_LEGACY_MODE,
        @SerializedName("V2_WINDOW_MODE")
        V2_WINDOW_MODE
    }
}
