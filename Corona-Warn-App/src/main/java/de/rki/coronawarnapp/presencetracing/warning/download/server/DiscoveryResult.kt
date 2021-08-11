package de.rki.coronawarnapp.presencetracing.warning.download.server

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.HourInterval

@Keep
data class DiscoveryResult(
    @SerializedName("oldest") val oldest: HourInterval,
    @SerializedName("latest") val latest: HourInterval
)
