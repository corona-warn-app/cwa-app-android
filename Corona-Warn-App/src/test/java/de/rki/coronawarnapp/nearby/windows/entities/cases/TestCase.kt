package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.google.gson.annotations.SerializedName

data class TestCase(
    @SerializedName("description")
    val description: String,
    @SerializedName("expAgeOfMostRecentDateWithHighRisk")
    val expAgeOfMostRecentDateWithHighRisk: Long?,
    @SerializedName("expAgeOfMostRecentDateWithLowRisk")
    val expAgeOfMostRecentDateWithLowRisk: Long?,
    @SerializedName("expTotalMinimumDistinctEncountersWithHighRisk")
    val expTotalMinimumDistinctEncountersWithHighRisk: Int,
    @SerializedName("expTotalMinimumDistinctEncountersWithLowRisk")
    val expTotalMinimumDistinctEncountersWithLowRisk: Int,
    @SerializedName("expTotalRiskLevel")
    val expTotalRiskLevel: Int,
    @SerializedName("exposureWindows")
    val exposureWindows: List<JsonWindow>
)
