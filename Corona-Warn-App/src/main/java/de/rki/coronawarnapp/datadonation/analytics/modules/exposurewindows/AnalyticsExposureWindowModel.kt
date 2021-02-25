package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

data class AnalyticsExposureWindow(
    val calibrationConfidence: Int,
    val dateMillis: Long,
    val infectiousness: Int,
    val reportType: Int,
    val analyticsScanInstances: List<AnalyticsScanInstance>,
    val normalizedTime: Double,
    val transmissionRiskLevel: Int
)

data class AnalyticsScanInstance(
    val minAttenuation: Int,
    val typicalAttenuation: Int,
    val secondsSinceLastScan: Int
)
