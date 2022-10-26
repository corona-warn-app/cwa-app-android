package de.rki.coronawarnapp.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.minusDaysAtStartOfDayUtc
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDao
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.toLocalDateUtc
import java.time.Instant
import java.time.temporal.ChronoUnit

object RiskStorageTestData {

    private val ewCalculatedAt = Instant.ofEpochMilli(9999L)

    val ewRiskResult1Increased = PersistedRiskLevelResultDao(
        id = "id1",
        calculatedAt = ewCalculatedAt,
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

    val ewRiskResult2Low = PersistedRiskLevelResultDao(
        id = "id2",
        calculatedAt = ewCalculatedAt.minusMillis(1000L),
        failureReason = null,
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            totalMinimumDistinctEncountersWithLowRisk = 1,
            totalMinimumDistinctEncountersWithHighRisk = 2,
            mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
            mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
            numberOfDaysWithLowRisk = 5,
            numberOfDaysWithHighRisk = 6
        )
    )

    val ewAggregatedRiskResult = EwAggregatedRiskResult(
        totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        totalMinimumDistinctEncountersWithLowRisk = 1,
        totalMinimumDistinctEncountersWithHighRisk = 2,
        mostRecentDateWithLowRisk = Instant.ofEpochMilli(3),
        mostRecentDateWithHighRisk = Instant.ofEpochMilli(4),
        numberOfDaysWithLowRisk = 5,
        numberOfDaysWithHighRisk = 6
    )

    val ewRiskLevelResult = EwRiskLevelTaskResult(
        calculatedAt = ewCalculatedAt,
        ewAggregatedRiskResult = ewAggregatedRiskResult,
        exposureWindows = null
    )

    val ewDaoWrapper = PersistedExposureWindowDaoWrapper(
        exposureWindowDao = PersistedExposureWindowDao(
            id = 1,
            riskLevelResultId = "id1",
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

    val ewDayRisk = ExposureWindowDayRisk(
        dateMillisSinceEpoch = ewCalculatedAt.toEpochMilli(),
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )

    val ewPersistedAggregatedRiskPerDateResult = PersistedAggregatedRiskPerDateResult(
        dateMillisSinceEpoch = ewCalculatedAt.toEpochMilli(),
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )

    val ewRiskLevelResultWithAggregatedRiskPerDateResult = ewRiskLevelResult.copy(
        ewAggregatedRiskResult = ewAggregatedRiskResult.copy(
            exposureWindowDayRisks = listOf(ewDayRisk)
        )
    )

    // PT data

    private val ptCalculatedAt = ewCalculatedAt.plusMillis(100L)
    private const val maxCheckInAgeInDays = 10

    val ptDayRisk = PresenceTracingDayRisk(
        Instant.now().toLocalDateUtc(),
        RiskState.INCREASED_RISK
    )

    val ptResult1Low = PtRiskLevelResult(
        calculatedAt = ptCalculatedAt,
        calculatedFrom = ptCalculatedAt.minusDaysAtStartOfDayUtc(4).toInstant(),
        riskState = RiskState.LOW_RISK
    )
    val ptResult2Failed = PtRiskLevelResult(
        calculatedAt = ptCalculatedAt.minus(1000L, ChronoUnit.MILLIS),
        presenceTracingDayRisk = null,
        riskState = RiskState.CALCULATION_FAILED,
        calculatedFrom = ptCalculatedAt.minusMillis(1000L).minusDaysAtStartOfDayUtc(maxCheckInAgeInDays).toInstant()
    )
}
