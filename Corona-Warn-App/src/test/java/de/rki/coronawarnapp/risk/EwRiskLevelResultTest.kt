package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class EwRiskLevelResultTest : BaseTest() {

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
