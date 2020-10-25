package de.rki.coronawarnapp.nearby.modules.calculationtracker

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

@Keep
data class Calculation(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("startedAt") val startedAt: Instant,
    @SerializedName("result") val result: Result? = null,
    @SerializedName("finishedAt") val finishedAt: Instant? = null
) {

    @Transient val isCalculating: Boolean = finishedAt == null
    @Transient val isSuccessful: Boolean =
        (result == Result.NO_MATCHES || result == Result.UPDATED_STATE)

    @Keep
    enum class Result {
        @SerializedName("NO_MATCHES")
        NO_MATCHES,

        @SerializedName("UPDATED_STATE")
        UPDATED_STATE,

        @SerializedName("TIMEOUT")
        TIMEOUT
    }
}
