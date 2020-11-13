package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest

class RiskLevelsTest : BaseTest() {

    private lateinit var riskLevels: DefaultRiskLevels

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        riskLevels = DefaultRiskLevels()
    }

    @Test
    fun `is within defined level threshold`() {
        riskLevels.withinDefinedLevelThreshold(2.0, 1, 3) shouldBe true
    }

    @Test
    fun `is not within defined level threshold`() {
        riskLevels.withinDefinedLevelThreshold(4.0, 1, 3) shouldBe false
    }

    @Test
    fun `is within defined level threshold - edge cases`() {
        riskLevels.withinDefinedLevelThreshold(1.0, 1, 3) shouldBe true
        riskLevels.withinDefinedLevelThreshold(3.0, 1, 3) shouldBe true
    }

    @Test
    fun calculateRiskScoreZero() {
        val riskScore =
            riskLevels.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(0, 0, 0, 0)
            )

        assertEquals(0.0, riskScore)
    }

    @Test
    fun calculateRiskScoreLow() {
        val riskScore =
            riskLevels.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(156, 10, 10, 10)
            )

        assertEquals(124.8, riskScore)
    }

    @Test
    fun calculateRiskScoreMid() {
        val riskScore =
            riskLevels.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(256, 15, 15, 15)
            )

        assertEquals(307.2, riskScore)
    }

    @Test
    fun calculateRiskScoreHigh() {
        val riskScore =
            riskLevels.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(512, 30, 30, 30)
            )

        assertEquals(1228.8, riskScore)
    }

    @Test
    fun calculateRiskScoreMax() {
        val riskScore =
            riskLevels.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(4096, 30, 30, 30)
            )

        assertEquals(9830.4, riskScore)
    }

    @Test
    fun calculateRiskScoreCapped() {
        val riskScore =
            riskLevels.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(4096, 45, 45, 45)
            )

        assertEquals(9830.4, riskScore)
    }

    private fun buildAttenuationDuration(
        high: Double,
        mid: Double,
        low: Double,
        norm: Int = 25,
        offset: Int = 0
    ): AttenuationDurationOuterClass.AttenuationDuration {
        return AttenuationDurationOuterClass.AttenuationDuration
            .newBuilder()
            .setRiskScoreNormalizationDivisor(norm)
            .setDefaultBucketOffset(offset)
            .setWeights(
                AttenuationDurationOuterClass.Weights
                    .newBuilder()
                    .setHigh(high)
                    .setMid(mid)
                    .setLow(low)
                    .build()
            )
            .build()
    }

    private fun buildSummary(
        maxRisk: Int = 0,
        lowAttenuation: Int = 0,
        midAttenuation: Int = 0,
        highAttenuation: Int = 0
    ): ExposureSummary {
        val intArray = IntArray(3)
        intArray[0] = lowAttenuation
        intArray[1] = midAttenuation
        intArray[2] = highAttenuation
        return ExposureSummary.ExposureSummaryBuilder()
            .setMaximumRiskScore(maxRisk)
            .setAttenuationDurations(intArray)
            .build()
    }
}
