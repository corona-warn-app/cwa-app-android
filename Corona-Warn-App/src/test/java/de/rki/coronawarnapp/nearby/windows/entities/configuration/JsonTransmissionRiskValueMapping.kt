package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonTransmissionRiskValueMapping(
    @JsonProperty("transmissionRiskLevel")
    val transmissionRiskLevel: Int,
    @JsonProperty("transmissionRiskValue")
    val transmissionRiskValue: Double
)
