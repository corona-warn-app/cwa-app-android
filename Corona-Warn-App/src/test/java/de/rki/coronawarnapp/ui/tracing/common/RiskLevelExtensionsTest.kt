package de.rki.coronawarnapp.ui.tracing.common

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelExtensionsTest : BaseTest() {

    private fun createRiskLevel(
        riskLevel: RiskLevel,
        calculatedAt: Instant
    ): RiskLevelResult = object : RiskLevelResult {
        override val riskLevel: RiskLevel = riskLevel
        override val calculatedAt: Instant = calculatedAt
        override val aggregatedRiskResult: AggregatedRiskResult? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    @Test
    fun `getLastestAndLastSuccessful on empty results`() {
        val emptyResults = emptyList<RiskLevelResult>()

        emptyResults.tryLatestResultsWithDefaults().apply {
            lastCalculated.apply {
                riskLevel shouldBe RiskLevel.LOW_LEVEL_RISK
                val now = Instant.now().millis
                calculatedAt.millis shouldBeInRange ((now - 60 * 1000L)..now + 60 * 1000L)
            }
            lastSuccessfullyCalculated.apply {
                riskLevel shouldBe RiskLevel.UNDETERMINED
            }
        }
    }

    @Test
    fun `getLastestAndLastSuccessful last calculation was successful`() {
        val results = listOf(
            createRiskLevel(RiskLevel.INCREASED_RISK, calculatedAt = Instant.EPOCH),
            createRiskLevel(RiskLevel.LOW_LEVEL_RISK, calculatedAt = Instant.EPOCH.plus(1)),
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.riskLevel shouldBe lastSuccessfullyCalculated.riskLevel
            lastCalculated.calculatedAt shouldBe lastSuccessfullyCalculated.calculatedAt

            lastCalculated.riskLevel shouldBe RiskLevel.LOW_LEVEL_RISK
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
        }
    }

    @Test
    fun `getLastestAndLastSuccessful last calculation was not successful`() {
        val results = listOf(
            createRiskLevel(RiskLevel.INCREASED_RISK, calculatedAt = Instant.EPOCH),
            createRiskLevel(RiskLevel.LOW_LEVEL_RISK, calculatedAt = Instant.EPOCH.plus(1)),
            createRiskLevel(RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF, calculatedAt = Instant.EPOCH.plus(2)),
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.riskLevel shouldBe RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(2)

            lastSuccessfullyCalculated.riskLevel shouldBe RiskLevel.LOW_LEVEL_RISK
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
        }
    }

    @Test
    fun `getLastestAndLastSuccessful no successful calculations yet`() {
        val results = listOf(
            createRiskLevel(RiskLevel.UNDETERMINED, calculatedAt = Instant.EPOCH.plus(10)),
            createRiskLevel(RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL, calculatedAt = Instant.EPOCH.plus(11)),
            createRiskLevel(RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS, calculatedAt = Instant.EPOCH.plus(12)),
            createRiskLevel(RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF, calculatedAt = Instant.EPOCH.plus(13)),
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.riskLevel shouldBe RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(13)

            lastSuccessfullyCalculated.riskLevel shouldBe RiskLevel.UNDETERMINED
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH
        }
    }
}
