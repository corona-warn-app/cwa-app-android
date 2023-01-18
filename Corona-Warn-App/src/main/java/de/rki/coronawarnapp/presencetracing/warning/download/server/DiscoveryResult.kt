package de.rki.coronawarnapp.presencetracing.warning.download.server

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.util.HourInterval

data class DiscoveryResult(
    @JsonProperty("oldest") val oldest: HourInterval,
    @JsonProperty("latest") val latest: HourInterval
)
