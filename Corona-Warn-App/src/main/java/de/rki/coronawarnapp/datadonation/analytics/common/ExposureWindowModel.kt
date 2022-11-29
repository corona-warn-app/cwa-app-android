package de.rki.coronawarnapp.datadonation.analytics.common

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

// internal data structures used to store data -- do not modify
data class AnalyticsExposureWindow(
    @SerializedName("calibrationConfidence")
    val calibrationConfidence: Int,
    @SerializedName("dateMillis")
    val dateMillis: Long,
    @SerializedName("infectiousness")
    val infectiousness: Int,
    @SerializedName("reportType")
    val reportType: Int,
    @SerializedName("analyticsScanInstances")
    val analyticsScanInstances: List<AnalyticsScanInstance>,
    @SerializedName("normalizedTime")
    val normalizedTime: Double,
    @SerializedName("transmissionRiskLevel")
    val transmissionRiskLevel: Int
) {
    fun sha256Hash() = toString().toSHA256()
}

data class AnalyticsScanInstance(
    @SerializedName("minAttenuation")
    val minAttenuation: Int,
    @SerializedName("typicalAttenuation")
    val typicalAttenuation: Int,
    @SerializedName("secondsSinceLastScan")
    val secondsSinceLastScan: Int
)
