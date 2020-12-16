package de.rki.coronawarnapp.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDao
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Instant

object RiskStorageTestData {

    val testRiskLevelResultDao = PersistedRiskLevelResultDao(
        id = "riskresult-id",
        calculatedAt = Instant.ofEpochMilli(9999L),
        failureReason = null,
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        )
    )

    val testRisklevelResult = RiskLevelTaskResult(
        calculatedAt = Instant.ofEpochMilli(9999L),
        aggregatedRiskResult = AggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        ),
        exposureWindows = null
    )

    val testExposureWindowDaoWrapper = PersistedExposureWindowDaoWrapper(
        exposureWindowDao = PersistedExposureWindowDao(
            id = 1,
            riskLevelResultId = "riskresult-id",
            dateMillisSinceEpoch = 123L,
            calibrationConfidence = 1,
            infectiousness = 2,
            reportType = 3
        ),
        scanInstances = listOf(
            PersistedExposureWindowDao.PersistedScanInstance(
                exposureWindowId = 1,
                minAttenuationDb = 10,
                secondsSinceLastScan = 20,
                typicalAttenuationDb = 30
            )
        )
    )
    val testExposureWindow: ExposureWindow = ExposureWindow.Builder().apply {
        setDateMillisSinceEpoch(123L)
        setCalibrationConfidence(1)
        setInfectiousness(2)
        setReportType(3)
        ScanInstance.Builder().apply {
            setMinAttenuationDb(10)
            setSecondsSinceLastScan(20)
            setTypicalAttenuationDb(30)
        }.build().let { setScanInstances(listOf(it)) }
    }.build()
}
