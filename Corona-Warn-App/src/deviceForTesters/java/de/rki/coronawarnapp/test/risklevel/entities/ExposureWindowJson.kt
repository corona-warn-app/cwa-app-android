package de.rki.coronawarnapp.test.risklevel.entities

import com.google.android.gms.nearby.exposurenotification.ExposureWindow

data class ExposureWindowJson(
    val dateMillisSinceEpoch: Long,
    val reportType: Int,
    val infectiousness: Int,
    val calibrationConfidence: Int,
    val scanInstances: List<ScanInstanceJson>
)

fun ExposureWindow.toExposureWindowJson(): ExposureWindowJson = ExposureWindowJson(
    dateMillisSinceEpoch = dateMillisSinceEpoch,
    reportType = reportType,
    infectiousness = infectiousness,
    calibrationConfidence = calibrationConfidence,
    scanInstances = scanInstances.map { it.toScanInstanceJson() }
)
