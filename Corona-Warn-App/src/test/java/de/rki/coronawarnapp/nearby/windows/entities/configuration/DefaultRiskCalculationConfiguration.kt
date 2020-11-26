package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class DefaultRiskCalculationConfiguration(
    @SerializedName("minutesAtAttenuationFilters")
    val minutesAtAttenuationFilters: List<JsonMinutesAtAttenuationFilter>,
    @SerializedName("minutesAtAttenuationWeights")
    val minutesAtAttenuationWeights: List<JsonMinutesAtAttenuationWeight>,
    @SerializedName("normalizedTimePerDayToRiskLevelMapping")
    val normalizedTimePerDayToRiskLevelMapping: List<JsonNormalizedTimeToRiskLevelMapping>,
    @SerializedName("normalizedTimePerEWToRiskLevelMapping")
    val normalizedTimePerEWToRiskLevelMapping: List<JsonNormalizedTimeToRiskLevelMapping>,
    @SerializedName("transmissionRiskLevelMultiplier")
    val transmissionRiskLevelMultiplier: Double,
    @SerializedName("trlEncoding")
    val trlEncoding: JsonTrlEncoding,
    @SerializedName("trlFilters")
    val trlFilters: List<JsonTrlFilter>
)
