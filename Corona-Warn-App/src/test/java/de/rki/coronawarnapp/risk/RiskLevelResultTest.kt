package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.Test
import testhelpers.BaseTest

class RiskLevelResultTest : BaseTest() {

    private fun createRiskLevel(
        aggregatedRiskResult: AggregatedRiskResult?,
        failureReason: RiskLevelResult.FailureReason?
    ): RiskLevelResult = object : RiskLevelResult {
        override val calculatedAt: Instant = Instant.EPOCH
        override val aggregatedRiskResult: AggregatedRiskResult? = aggregatedRiskResult
        override val failureReason: RiskLevelResult.FailureReason? = failureReason
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    @Test
    fun testUnsuccessfulRistLevels() {
        createRiskLevel(
            aggregatedRiskResult = null,
            failureReason = RiskLevelResult.FailureReason.UNKNOWN
        ).wasSuccessfullyCalculated shouldBe false

        createRiskLevel(
            aggregatedRiskResult = mockk(),
            failureReason = null
        ).wasSuccessfullyCalculated shouldBe true
    }
}
