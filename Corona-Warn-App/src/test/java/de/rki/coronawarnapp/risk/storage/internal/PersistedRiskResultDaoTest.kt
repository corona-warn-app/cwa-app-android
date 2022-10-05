package de.rki.coronawarnapp.risk.storage.internal

import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.ewDaoWrapper
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindow
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class PersistedRiskResultDaoTest : BaseTest() {

    @Test
    fun `mapping successful result`() {
        PersistedRiskLevelResultDao(
            id = "",
            calculatedAt = Instant.ofEpochMilli(931161601L),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
                totalMinimumDistinctEncountersWithLowRisk = 89,
                totalMinimumDistinctEncountersWithHighRisk = 59,
                mostRecentDateWithLowRisk = Instant.ofEpochMilli(852191241L),
                mostRecentDateWithHighRisk = Instant.ofEpochMilli(790335113L),
                numberOfDaysWithLowRisk = 52,
                numberOfDaysWithHighRisk = 81
            )
        ).toRiskResult(listOf(ewDaoWrapper)).apply {
            riskState shouldBe RiskState.LOW_RISK
            calculatedAt.toEpochMilli() shouldBe 931161601L
            exposureWindows shouldBe listOf(testExposureWindow)
            failureReason shouldBe null
            ewAggregatedRiskResult shouldNotBe null
            ewAggregatedRiskResult?.apply {
                totalRiskLevel shouldBe
                    RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
                totalMinimumDistinctEncountersWithLowRisk shouldBe 89
                totalMinimumDistinctEncountersWithHighRisk shouldBe 59
                mostRecentDateWithLowRisk shouldNotBe null
                mostRecentDateWithLowRisk?.toEpochMilli() shouldBe 852191241L
                mostRecentDateWithHighRisk shouldNotBe null
                mostRecentDateWithHighRisk?.toEpochMilli() shouldBe 790335113L
                numberOfDaysWithLowRisk shouldBe 52
                numberOfDaysWithHighRisk shouldBe 81
            }
        }
    }

    @Test
    fun `mapping successful result with exposure windows`() {
        PersistedRiskLevelResultDao(
            id = "",
            calculatedAt = Instant.ofEpochMilli(931161601L),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
                totalMinimumDistinctEncountersWithLowRisk = 89,
                totalMinimumDistinctEncountersWithHighRisk = 59,
                mostRecentDateWithLowRisk = Instant.ofEpochMilli(852191241L),
                mostRecentDateWithHighRisk = Instant.ofEpochMilli(790335113L),
                numberOfDaysWithLowRisk = 52,
                numberOfDaysWithHighRisk = 81
            )
        ).toRiskResult().apply {
            riskState shouldBe RiskState.LOW_RISK
            calculatedAt.toEpochMilli() shouldBe 931161601L
            exposureWindows shouldBe null
            failureReason shouldBe null
            ewAggregatedRiskResult shouldNotBe null
            ewAggregatedRiskResult?.apply {
                totalRiskLevel shouldBe
                    RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
                totalMinimumDistinctEncountersWithLowRisk shouldBe 89
                totalMinimumDistinctEncountersWithHighRisk shouldBe 59
                mostRecentDateWithLowRisk shouldNotBe null
                mostRecentDateWithLowRisk?.toEpochMilli() shouldBe 852191241L
                mostRecentDateWithHighRisk shouldNotBe null
                mostRecentDateWithHighRisk?.toEpochMilli() shouldBe 790335113L
                numberOfDaysWithLowRisk shouldBe 52
                numberOfDaysWithHighRisk shouldBe 81
            }
        }
    }

    @Test
    fun `mapping failed result`() {
        PersistedRiskLevelResultDao(
            id = "",
            calculatedAt = Instant.ofEpochMilli(931161601L),
            failureReason = EwRiskLevelResult.FailureReason.TRACING_OFF,
            aggregatedRiskResult = null
        ).toRiskResult().apply {
            riskState shouldBe RiskState.CALCULATION_FAILED
            calculatedAt.toEpochMilli() shouldBe 931161601L
            exposureWindows shouldBe null
            failureReason shouldBe EwRiskLevelResult.FailureReason.TRACING_OFF
            ewAggregatedRiskResult shouldBe null
        }
    }
}
