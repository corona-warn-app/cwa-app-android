package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.fasterxml.jackson.annotation.JsonProperty

data class TestCase(
    @JsonProperty("description")
    val description: String,
    @JsonProperty("expAgeOfMostRecentDateWithHighRisk")
    val expAgeOfMostRecentDateWithHighRiskInDays: Long?,
    @JsonProperty("expAgeOfMostRecentDateWithLowRisk")
    val expAgeOfMostRecentDateWithLowRiskInDays: Long?,
    @JsonProperty("expTotalMinimumDistinctEncountersWithHighRisk")
    val expTotalMinimumDistinctEncountersWithHighRisk: Int,
    @JsonProperty("expTotalMinimumDistinctEncountersWithLowRisk")
    val expTotalMinimumDistinctEncountersWithLowRisk: Int,
    @JsonProperty("expTotalRiskLevel")
    val expTotalRiskLevel: Int,
    @JsonProperty("expNumberOfDaysWithLowRisk")
    val expNumberOfDaysWithLowRisk: Int,
    @JsonProperty("expNumberOfDaysWithHighRisk")
    val expNumberOfDaysWithHighRisk: Int,
    @JsonProperty("exposureWindows")
    val exposureWindows: List<JsonWindow>
)
