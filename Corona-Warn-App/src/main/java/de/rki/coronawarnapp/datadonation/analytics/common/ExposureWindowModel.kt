package de.rki.coronawarnapp.datadonation.analytics.common

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

// internal data structures used to store data -- do not modify
data class AnalyticsExposureWindow(
    @JsonProperty("calibrationConfidence")
    val calibrationConfidence: Int,
    @JsonProperty("dateMillis")
    val dateMillis: Long,
    @JsonProperty("infectiousness")
    val infectiousness: Int,
    @JsonProperty("reportType")
    val reportType: Int,
    @JsonProperty("analyticsScanInstances")
    val analyticsScanInstances: List<AnalyticsScanInstance>,
    @JsonProperty("normalizedTime")
    val normalizedTime: Double,
    @JsonProperty("transmissionRiskLevel")
    val transmissionRiskLevel: Int
) {
    fun sha256Hash() = toString().toSHA256()
}

data class AnalyticsScanInstance(
    @JsonProperty("minAttenuation")
    val minAttenuation: Int,
    @JsonProperty("typicalAttenuation")
    val typicalAttenuation: Int,
    @JsonProperty("secondsSinceLastScan")
    val secondsSinceLastScan: Int
)
