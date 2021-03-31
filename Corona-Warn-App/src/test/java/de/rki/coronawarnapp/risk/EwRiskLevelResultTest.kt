package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.Test
import testhelpers.BaseTest

class EwRiskLevelResultTest : BaseTest() {

    private fun createRiskLevel(
        ewAggregatedRiskResult: EwAggregatedRiskResult?,
        failureReason: EwRiskLevelResult.FailureReason?
    ): EwRiskLevelResult = object : EwRiskLevelResult {
        override val calculatedAt: Instant = Instant.EPOCH
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = ewAggregatedRiskResult
        override val failureReason: EwRiskLevelResult.FailureReason? = failureReason
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    @Test
    fun testUnsuccessfulRistLevels() {
        createRiskLevel(
            ewAggregatedRiskResult = null,
            failureReason = EwRiskLevelResult.FailureReason.UNKNOWN
        ).wasSuccessfullyCalculated shouldBe false

        createRiskLevel(
            ewAggregatedRiskResult = mockk(),
            failureReason = null
        ).wasSuccessfullyCalculated shouldBe true
    }
}
