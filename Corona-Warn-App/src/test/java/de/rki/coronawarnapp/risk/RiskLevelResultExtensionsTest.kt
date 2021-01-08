package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelResultExtensionsTest : BaseTest() {

    private fun createRiskLevelResult(
        hasResult: Boolean,
        calculatedAt: Instant
    ): RiskLevelResult = object : RiskLevelResult {
        override val calculatedAt: Instant = calculatedAt
        override val aggregatedRiskResult: AggregatedRiskResult? = if (hasResult) mockk() else null
        override val failureReason: RiskLevelResult.FailureReason?
            get() = if (!hasResult) RiskLevelResult.FailureReason.UNKNOWN else null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    @Test
    fun `getLastestAndLastSuccessful on empty results`() {
        val emptyResults = emptyList<RiskLevelResult>()

        emptyResults.tryLatestResultsWithDefaults().apply {
            lastCalculated.apply {
                riskState shouldBe RiskState.LOW_RISK
                val now = Instant.now().millis
                calculatedAt.millis shouldBeInRange ((now - 60 * 1000L)..now + 60 * 1000L)
            }
            lastSuccessfullyCalculated.apply {
                riskState shouldBe RiskState.CALCULATION_FAILED
            }
        }
    }

    @Test
    fun `getLastestAndLastSuccessful last calculation was successful`() {
        val results = listOf(
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH),
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH.plus(1))
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
        }
    }

    @Test
    fun `getLastestAndLastSuccessful last calculation was not successful`() {
        val results = listOf(
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH),
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH.plus(1)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plus(2))
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(2)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
        }
    }

    @Test
    fun `getLastestAndLastSuccessful no successful calculations yet`() {
        val results = listOf(
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plus(10)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plus(11)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plus(12)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plus(13))
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(13)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH
        }
    }
}
