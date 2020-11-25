package de.rki.coronawarnapp.risk.storage.internal.windows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance

fun ExposureWindow.toPersistedExposureWindow(
    riskLevelResultId: String
) = PersistedExposureWindowDao(
    riskLevelResultId = riskLevelResultId,
    dateMillisSinceEpoch = this.dateMillisSinceEpoch,
    calibrationConfidence = this.calibrationConfidence,
    infectiousness = this.infectiousness,
    reportType = this.reportType
)

fun List<ExposureWindow>.toPersistedExposureWindows(
    riskLevelResultId: String
) = this.map { it.toPersistedExposureWindow(riskLevelResultId) }

fun ScanInstance.toPersistedScanInstance(exposureWindowId: Long) = PersistedExposureWindowDao.PersistedScanInstance(
    exposureWindowId = exposureWindowId,
    minAttenuationDb = minAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan,
    typicalAttenuationDb = typicalAttenuationDb
)

fun List<ScanInstance>.toPersistedScanInstances(
    exposureWindowId: Long
) = this.map { it.toPersistedScanInstance(exposureWindowId) }
