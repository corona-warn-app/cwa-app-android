package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import junit.framework.TestCase.assertEquals
import org.junit.Test

class RiskLevelCalculationTest {

    @Test
    fun calculateRiskScoreZero() {
        val riskScore =
            RiskLevelCalculation.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(0, 0, 0, 0)
            )

        assertEquals(0.0, riskScore)
    }

    @Test
    fun calculateRiskScoreLow() {
        val riskScore =
            RiskLevelCalculation.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(156, 10, 10, 10)
            )

        assertEquals(124.8, riskScore)
    }

    @Test
    fun calculateRiskScoreMid() {
        val riskScore =
            RiskLevelCalculation.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(256, 15, 15, 15)
            )

        assertEquals(307.2, riskScore)
    }

    @Test
    fun calculateRiskScoreHigh() {
        val riskScore =
            RiskLevelCalculation.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(512, 30, 30, 30)
            )

        assertEquals(1228.8, riskScore)
    }

    @Test
    fun calculateRiskScoreMax() {
        val riskScore =
            RiskLevelCalculation.calculateRiskScore(
                buildAttenuationDuration(0.5, 0.5, 1.0),
                buildSummary(4096, 30, 30, 30)
            )

        assertEquals(9830.4, riskScore)
    }

    @Test
    fun calculateRiskScoreCapped() {
        val riskScore =
            RiskLevelCalculation.calculateRiskScore(
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
    ): ApplicationConfigurationOuterClass.AttenuationDuration {
        return ApplicationConfigurationOuterClass.AttenuationDuration
            .newBuilder()
            .setRiskScoreNormalizationDivisor(norm)
            .setDefaultBucketOffset(offset)
            .setWeights(
                ApplicationConfigurationOuterClass.Weights
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
