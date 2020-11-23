package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.Test
import testhelpers.BaseTest

class RiskLevelResultTest : BaseTest() {

    private fun createRiskLevel(riskLevel: RiskLevel): RiskLevelResult = object : RiskLevelResult {
        override val riskLevel: RiskLevel = riskLevel
        override val calculatedAt: Instant = Instant.EPOCH
        override val aggregatedRiskResult: AggregatedRiskResult? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val isIncreasedRisk: Boolean = false
        override val matchedKeyCount: Int = 0
        override val daysSinceLastExposure: Int = 0
    }

    @Test
    fun testUnsuccessfulRistLevels() {
        createRiskLevel(RiskLevel.UNDETERMINED).wasSuccessfullyCalculated shouldBe false
        createRiskLevel(RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF).wasSuccessfullyCalculated shouldBe false
        createRiskLevel(RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS).wasSuccessfullyCalculated shouldBe false

        createRiskLevel(RiskLevel.UNKNOWN_RISK_INITIAL).wasSuccessfullyCalculated shouldBe true
        createRiskLevel(RiskLevel.LOW_LEVEL_RISK).wasSuccessfullyCalculated shouldBe true
        createRiskLevel(RiskLevel.INCREASED_RISK).wasSuccessfullyCalculated shouldBe true
    }
}
