package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonMinutesAtAttenuationWeight(
    @JsonProperty("attenuationRange")
    val attenuationRange: Range,
    @JsonProperty("weight")
    val weight: Double
)
