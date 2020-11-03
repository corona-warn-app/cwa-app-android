package de.rki.coronawarnapp.nearby.windows.entities.cases


import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.nearby.windows.entities.cases.ExposureWindow

data class TestCase(
    @SerializedName("description")
    val description: String,
    @SerializedName("expAgeOfMostRecentDateWithHighRisk")
    val expAgeOfMostRecentDateWithHighRisk: Any,
    @SerializedName("expAgeOfMostRecentDateWithLowRisk")
    val expAgeOfMostRecentDateWithLowRisk: Any,
    @SerializedName("expNumberOfExposureWindowsWithHighRisk")
    val expNumberOfExposureWindowsWithHighRisk: Int,
    @SerializedName("expNumberOfExposureWindowsWithLowRisk")
    val expNumberOfExposureWindowsWithLowRisk: Int,
    @SerializedName("expTotalMinimumDistinctEncountersWithHighRisk")
    val expTotalMinimumDistinctEncountersWithHighRisk: Int,
    @SerializedName("expTotalMinimumDistinctEncountersWithLowRisk")
    val expTotalMinimumDistinctEncountersWithLowRisk: Int,
    @SerializedName("expTotalRiskLevel")
    val expTotalRiskLevel: Int,
    @SerializedName("exposureWindows")
    val exposureWindows: List<ExposureWindow>
)
