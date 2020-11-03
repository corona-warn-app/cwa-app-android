package de.rki.coronawarnapp.nearby.windows.entities.configuration


import com.google.gson.annotations.SerializedName

data class NormalizedTimePerDayToRiskLevelMapping(
    @SerializedName("normalizedTimeRange")
    val normalizedTimeRange: Range,
    @SerializedName("riskLevel")
    val riskLevel: Int
)
