package de.rki.coronawarnapp.nearby.modules.calculationtracker

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

@Keep
data class Calculation(
    @SerializedName("token") val token: String,
    @SerializedName("state") val state: State = State.CALCULATING,
    @SerializedName("startedAt") val startedAt: Instant,
    @SerializedName("result") val result: Result? = null,
    @SerializedName("finishedAt") val finishedAt: Instant? = null
) {
    @Keep
    enum class State {
        @SerializedName("CALCULATING")
        CALCULATING,

        @SerializedName("DONE")
        DONE
    }

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
