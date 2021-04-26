package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class EwRiskLevelResultTest : BaseTest() {

    @MockK lateinit var ewAggregatedRiskResult1: EwAggregatedRiskResult

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testUnsuccessfulRiskLevels() {
        createRiskLevel(
            ewAggregatedRiskResult = null,
            failureReason = EwRiskLevelResult.FailureReason.UNKNOWN
        ).wasSuccessfullyCalculated shouldBe false

        createRiskLevel(
            ewAggregatedRiskResult = mockk(),
            failureReason = null
        ).wasSuccessfullyCalculated shouldBe true
    }

    @Test
    fun `counts days correctly`() {
        val dayRisk = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000,
            riskLevel = RiskLevel.HIGH,
            minimumDistinctEncountersWithLowRisk = 0,
            minimumDistinctEncountersWithHighRisk = 1
        )
        val dayRisk2 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + MILLIS_DAY,
            riskLevel = RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 0
        )
        val dayRisk3 = ExposureWindowDayRisk(
            dateMillisSinceEpoch = 1000 + 2 * MILLIS_DAY,
            riskLevel = RiskLevel.HIGH,
            minimumDistinctEncountersWithLowRisk = 1,
            minimumDistinctEncountersWithHighRisk = 2
        )
        every { ewAggregatedRiskResult1.exposureWindowDayRisks } returns listOf(dayRisk, dayRisk2, dayRisk3)
        val riskLevel = createRiskLevel(
            ewAggregatedRiskResult = ewAggregatedRiskResult1,
            failureReason = null
        )
        riskLevel.daysWithHighRisk.size shouldBe 2
        riskLevel.daysWithLowRisk.size shouldBe 1
    }

    private fun createRiskLevel(
        ewAggregatedRiskResult: EwAggregatedRiskResult?,
        failureReason: EwRiskLevelResult.FailureReason?
    ): EwRiskLevelResult = object : EwRiskLevelResult {
        override val calculatedAt: Instant = Instant.EPOCH
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = ewAggregatedRiskResult
        override val failureReason: EwRiskLevelResult.FailureReason? = failureReason
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
    }
}

private const val MILLIS_DAY = (1000 * 60 * 60 * 24).toLong()
