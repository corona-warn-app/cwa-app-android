package de.rki.coronawarnapp.nearby.windows.entities.configuration


import com.google.gson.annotations.SerializedName

data class DefaultRiskCalculationConfiguration(
    @SerializedName("minutesAtAttenuationFilters")
    val minutesAtAttenuationFilters: List<MinutesAtAttenuationFilter>,
    @SerializedName("minutesAtAttenuationWeights")
    val minutesAtAttenuationWeights: List<MinutesAtAttenuationWeight>,
    @SerializedName("normalizedTimePerDayToRiskLevelMapping")
    val normalizedTimePerDayToRiskLevelMapping: List<NormalizedTimePerDayToRiskLevelMapping>,
    @SerializedName("normalizedTimePerEWToRiskLevelMapping")
    val normalizedTimePerEWToRiskLevelMapping: List<NormalizedTimePerEWToRiskLevelMapping>,
    @SerializedName("transmissionRiskLevelMultiplier")
    val transmissionRiskLevelMultiplier: Double,
    @SerializedName("trlEncoding")
    val trlEncoding: TrlEncoding,
    @SerializedName("trlFilters")
    val trlFilters: List<TrlFilter>
)
