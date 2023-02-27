package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class DefaultRiskCalculationConfiguration(
    @JsonProperty("minutesAtAttenuationFilters")
    val minutesAtAttenuationFilters: List<JsonMinutesAtAttenuationFilter>,
    @JsonProperty("minutesAtAttenuationWeights")
    val minutesAtAttenuationWeights: List<JsonMinutesAtAttenuationWeight>,
    @JsonProperty("normalizedTimePerDayToRiskLevelMapping")
    val normalizedTimePerDayToRiskLevelMapping: List<JsonNormalizedTimeToRiskLevelMapping>,
    @JsonProperty("normalizedTimePerEWToRiskLevelMapping")
    val normalizedTimePerEWToRiskLevelMapping: List<JsonNormalizedTimeToRiskLevelMapping>,
    @JsonProperty("trlEncoding")
    val trlEncoding: JsonTrlEncoding,
    @JsonProperty("trlFilters")
    val trlFilters: List<JsonTrlFilter>,
    @JsonProperty("transmissionRiskValueMapping")
    val transmissionRiskValueMapping: List<JsonTransmissionRiskValueMapping>
)
