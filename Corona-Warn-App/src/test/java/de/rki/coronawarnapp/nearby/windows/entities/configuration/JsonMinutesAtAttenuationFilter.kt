package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonMinutesAtAttenuationFilter(
    @JsonProperty("attenuationRange")
    val attenuationRange: Range,
    @JsonProperty("dropIfMinutesInRange")
    val dropIfMinutesInRange: Range
)
