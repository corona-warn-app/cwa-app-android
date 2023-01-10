package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonNormalizedTimeToRiskLevelMapping(
    @JsonProperty("normalizedTimeRange")
    val normalizedTimeRange: Range,
    @JsonProperty("riskLevel")
    val riskLevel: Int
)
