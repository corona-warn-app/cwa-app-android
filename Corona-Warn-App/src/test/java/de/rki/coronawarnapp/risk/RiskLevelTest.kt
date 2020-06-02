package de.rki.coronawarnapp.risk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskLevelTest {

    @Test
    fun testRiskLevelChangedFromHighToHigh() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.INCREASED_RISK,
            RiskLevel.INCREASED_RISK
        )
        assertFalse(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromLowToLow() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.UNKNOWN_RISK_INITIAL,
            RiskLevel.LOW_LEVEL_RISK
        )
        assertFalse(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromLowToHigh() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.UNKNOWN_RISK_INITIAL,
            RiskLevel.INCREASED_RISK
        )
        assertTrue(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromHighToLow() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.INCREASED_RISK,
            RiskLevel.UNKNOWN_RISK_INITIAL
        )
        assertTrue(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromUndeterminedToLow() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.UNDETERMINED,
            RiskLevel.UNKNOWN_RISK_INITIAL
        )
        assertFalse(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromUndeterminedToHigh() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.UNDETERMINED,
            RiskLevel.INCREASED_RISK
        )
        assertTrue(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromLowToUndetermined() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.UNKNOWN_RISK_INITIAL,
            RiskLevel.UNDETERMINED
        )
        assertFalse(riskLevelHasChanged)
    }

    @Test
    fun testRiskLevelChangedFromHighToUndetermined() {
        val riskLevelHasChanged = RiskLevel.riskLevelChangedBetweenLowAndHigh(
            RiskLevel.INCREASED_RISK,
            RiskLevel.UNDETERMINED
        )
        assertTrue(riskLevelHasChanged)
    }
}
