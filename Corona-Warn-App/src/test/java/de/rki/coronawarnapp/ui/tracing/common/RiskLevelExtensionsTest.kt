package de.rki.coronawarnapp.ui.tracing.common

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelExtensionsTest : BaseTest() {

    private fun createRiskLevel(
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
            createRiskLevel(hasResult = true, calculatedAt = Instant.EPOCH),
            createRiskLevel(hasResult = true, calculatedAt = Instant.EPOCH.plus(1))
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
        }
    }

    @Test
    fun `getLastestAndLastSuccessful last calculation was not successful`() {
        val results = listOf(
            createRiskLevel(hasResult = true, calculatedAt = Instant.EPOCH),
            createRiskLevel(hasResult = true, calculatedAt = Instant.EPOCH.plus(1)),
            createRiskLevel(hasResult = false, calculatedAt = Instant.EPOCH.plus(2))
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(2)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plus(1)
        }
    }

    @Test
    fun `getLastestAndLastSuccessful no successful calculations yet`() {
        val results = listOf(
            createRiskLevel(hasResult = false, calculatedAt = Instant.EPOCH.plus(10)),
            createRiskLevel(hasResult = false, calculatedAt = Instant.EPOCH.plus(11)),
            createRiskLevel(hasResult = false, calculatedAt = Instant.EPOCH.plus(12)),
            createRiskLevel(hasResult = false, calculatedAt = Instant.EPOCH.plus(13))
        )

        results.tryLatestResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plus(13)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH
        }
    }
}
