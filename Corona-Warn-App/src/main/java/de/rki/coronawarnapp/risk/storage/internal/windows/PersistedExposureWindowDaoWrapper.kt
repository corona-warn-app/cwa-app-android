package de.rki.coronawarnapp.risk.storage.internal.windows

import androidx.room.Embedded
import androidx.room.Relation
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance

/**
 * Helper class for Room @Relation
 */
data class PersistedExposureWindowDaoWrapper(
    @Embedded
    val exposureWindowDao: PersistedExposureWindowDao,
    @Relation(parentColumn = "id", entityColumn = "exposureWindowId")
    val scanInstances: List<PersistedExposureWindowDao.PersistedScanInstance>
) {
    fun toExposureWindow(): ExposureWindow =
        ExposureWindow.Builder().apply {
            setDateMillisSinceEpoch(exposureWindowDao.dateMillisSinceEpoch)
            setCalibrationConfidence(exposureWindowDao.calibrationConfidence)
            setInfectiousness(exposureWindowDao.infectiousness)
            setReportType(exposureWindowDao.reportType)
            setScanInstances(scanInstances.map { it.toScanInstance() })
        }.build()

    private fun PersistedExposureWindowDao.PersistedScanInstance.toScanInstance(): ScanInstance = ScanInstance.Builder()
        .apply {
            setMinAttenuationDb(minAttenuationDb)
            setSecondsSinceLastScan(secondsSinceLastScan)
            setTypicalAttenuationDb(typicalAttenuationDb)
        }.build()
}
