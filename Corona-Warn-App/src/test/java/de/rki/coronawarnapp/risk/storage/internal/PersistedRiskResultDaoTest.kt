package de.rki.coronawarnapp.risk.storage.internal

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindow
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindowDaoWrapper
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

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
        ).toRiskResult(listOf(testExposureWindowDaoWrapper)).apply {
            riskState shouldBe RiskState.LOW_RISK
            calculatedAt.millis shouldBe 931161601L
            exposureWindows shouldBe listOf(testExposureWindow)
            failureReason shouldBe null
            aggregatedRiskResult shouldNotBe null
            aggregatedRiskResult?.apply {
                totalRiskLevel shouldBe RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
                totalMinimumDistinctEncountersWithLowRisk shouldBe 89
                totalMinimumDistinctEncountersWithHighRisk shouldBe 59
                mostRecentDateWithLowRisk shouldNotBe null
                mostRecentDateWithLowRisk?.millis shouldBe 852191241L
                mostRecentDateWithHighRisk shouldNotBe null
                mostRecentDateWithHighRisk?.millis shouldBe 790335113L
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
            calculatedAt.millis shouldBe 931161601L
            exposureWindows shouldBe null
            failureReason shouldBe null
            aggregatedRiskResult shouldNotBe null
            aggregatedRiskResult?.apply {
                totalRiskLevel shouldBe RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW
                totalMinimumDistinctEncountersWithLowRisk shouldBe 89
                totalMinimumDistinctEncountersWithHighRisk shouldBe 59
                mostRecentDateWithLowRisk shouldNotBe null
                mostRecentDateWithLowRisk?.millis shouldBe 852191241L
                mostRecentDateWithHighRisk shouldNotBe null
                mostRecentDateWithHighRisk?.millis shouldBe 790335113L
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
            failureReason = RiskLevelResult.FailureReason.TRACING_OFF,
            aggregatedRiskResult = null
        ).toRiskResult().apply {
            riskState shouldBe RiskState.CALCULATION_FAILED
            calculatedAt.millis shouldBe 931161601L
            exposureWindows shouldBe null
            failureReason shouldBe RiskLevelResult.FailureReason.TRACING_OFF
            aggregatedRiskResult shouldBe null
        }
    }
}
