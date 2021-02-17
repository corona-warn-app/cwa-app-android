package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.risk.result.RiskResult

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

fun createAnalyticsExposureWindow(
    window: ExposureWindow,
    result: RiskResult
) = AnalyticsExposureWindow(
    calibrationConfidence = window.calibrationConfidence,
    dateMillis = window.dateMillisSinceEpoch,
    infectiousness = window.infectiousness,
    reportType = window.reportType,
    analyticsScanInstances = window.scanInstances.map { it.toAnalyticsScanInstance() },
    normalizedTime = result.normalizedTime,
    transmissionRiskLevel = result.transmissionRiskLevel
)

private fun ScanInstance.toAnalyticsScanInstance() = AnalyticsScanInstance(
    minAttenuation = minAttenuationDb,
    typicalAttenuation = typicalAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan
)
