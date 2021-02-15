package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.risk.result.RiskResult

data class ExposureWindowContribution(
    val calibrationConfidence: Int,
    val dateMillis: Long,
    val infectiousness: Int,
    val reportType: Int,
    val scanInstances: List<ScanInstanceContribution>,
    val normalizedTime: Double,
    val transmissionRiskLevel: Int
)

data class ScanInstanceContribution(
    val minAttenuation: Int,
    val typicalAttenuation: Int,
    val secondsSinceLastScan: Int
)

private fun ScanInstance.toScanInstanceContribution() = ScanInstanceContribution(
    minAttenuation = minAttenuationDb,
    typicalAttenuation = typicalAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan
)

fun createExposureWindowContribution(
    window: ExposureWindow,
    result: RiskResult
): ExposureWindowContribution =
    ExposureWindowContribution(
        calibrationConfidence = window.calibrationConfidence,
        dateMillis = window.dateMillisSinceEpoch,
        infectiousness = window.infectiousness,
        reportType = window.reportType,
        scanInstances = window.scanInstances.map { it.toScanInstanceContribution() },
        normalizedTime = result.normalizedTime,
        transmissionRiskLevel = result.transmissionRiskLevel
    )
