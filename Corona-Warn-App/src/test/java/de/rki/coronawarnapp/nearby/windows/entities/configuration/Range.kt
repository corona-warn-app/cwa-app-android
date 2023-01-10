package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class Range(
    @JsonProperty("min")
    val min: Double,
    @JsonProperty("minExclusive")
    val minExclusive: Boolean,
    @JsonProperty("max")
    val max: Double,
    @JsonProperty("maxExclusive")
    val maxExclusive: Boolean
)
