package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant

class EwRiskLevelResultExtensionsTest : BaseTest() {

    private fun createRiskLevelResult(
        hasResult: Boolean,
        calculatedAt: Instant
    ): EwRiskLevelResult = object : EwRiskLevelResult {
        override val calculatedAt: Instant = calculatedAt
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = if (hasResult) mockk() else null
        override val failureReason: EwRiskLevelResult.FailureReason?
            get() = if (!hasResult) EwRiskLevelResult.FailureReason.UNKNOWN else null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
    }

    @Test
    fun `getLatestAndLastSuccessful on empty results`() {
        val emptyResults = emptyList<EwRiskLevelResult>()

        emptyResults.tryLatestEwResultsWithDefaults().apply {
            lastCalculated.apply {
                riskState shouldBe RiskState.LOW_RISK

                calculatedAt.isAfter(Instant.EPOCH) shouldBe true
                calculatedAt.isBefore(Instant.now().plus(Duration.ofHours(1))) shouldBe true
                lastSuccessfullyCalculated.apply {
                    riskState shouldBe RiskState.CALCULATION_FAILED
                }
            }
        }
    }

    @Test
    fun `getLatestAndLastSuccessful last calculation was successful`() {
        val results = listOf(
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH),
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH.plusMillis(1))
        )

        results.tryLatestEwResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plusMillis(1)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plusMillis(1)
        }
    }

    @Test
    fun `getLatestAndLastSuccessful last calculation was not successful`() {
        val results = listOf(
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH),
            createRiskLevelResult(hasResult = true, calculatedAt = Instant.EPOCH.plusMillis(1)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plusMillis(2))
        )

        results.tryLatestEwResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plusMillis(2)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH.plusMillis(1)
        }
    }

    @Test
    fun `getLatestAndLastSuccessful no successful calculations yet`() {
        val results = listOf(
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plusMillis(10)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plusMillis(11)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plusMillis(12)),
            createRiskLevelResult(hasResult = false, calculatedAt = Instant.EPOCH.plusMillis(13))
        )

        results.tryLatestEwResultsWithDefaults().apply {
            lastCalculated.calculatedAt shouldBe Instant.EPOCH.plusMillis(13)
            lastSuccessfullyCalculated.calculatedAt shouldBe Instant.EPOCH
        }
    }
}
