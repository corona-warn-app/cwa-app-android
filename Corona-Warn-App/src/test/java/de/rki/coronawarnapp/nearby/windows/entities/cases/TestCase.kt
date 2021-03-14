package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.google.gson.annotations.SerializedName

data class TestCase(
    @SerializedName("description")
    val description: String,
    @SerializedName("expAgeOfMostRecentDateWithHighRisk")
    val expAgeOfMostRecentDateWithHighRiskInDays: Long?,
    @SerializedName("expAgeOfMostRecentDateWithLowRisk")
    val expAgeOfMostRecentDateWithLowRiskInDays: Long?,
    @SerializedName("expTotalMinimumDistinctEncountersWithHighRisk")
    val expTotalMinimumDistinctEncountersWithHighRisk: Int,
    @SerializedName("expTotalMinimumDistinctEncountersWithLowRisk")
    val expTotalMinimumDistinctEncountersWithLowRisk: Int,
    @SerializedName("expTotalRiskLevel")
    val expTotalRiskLevel: Int,
    @SerializedName("expNumberOfDaysWithLowRisk")
    val expNumberOfDaysWithLowRisk: Int,
    @SerializedName("expNumberOfDaysWithHighRisk")
    val expNumberOfDaysWithHighRisk: Int,
    @SerializedName("exposureWindows")
    val exposureWindows: List<JsonWindow>
)
