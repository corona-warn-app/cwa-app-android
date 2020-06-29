package de.rki.coronawarnapp.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskLevelTest {

    @Test
    fun testEnum() {
        assertEquals(RiskLevel.UNKNOWN_RISK_INITIAL.raw, RiskLevelConstants.UNKNOWN_RISK_INITIAL)
        assertEquals(
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF.raw,
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF
        )
        assertEquals(RiskLevel.LOW_LEVEL_RISK.raw, RiskLevelConstants.LOW_LEVEL_RISK)
        assertEquals(RiskLevel.INCREASED_RISK.raw, RiskLevelConstants.INCREASED_RISK)
        assertEquals(RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS.raw, RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        assertEquals(RiskLevel.UNDETERMINED.raw, RiskLevelConstants.UNDETERMINED)
    }

    @Test
    fun testForValue() {
        assertEquals(RiskLevel.forValue(RiskLevelConstants.UNKNOWN_RISK_INITIAL), RiskLevel.UNKNOWN_RISK_INITIAL)
        assertEquals(
            RiskLevel.forValue(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF),
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
        )
        assertEquals(RiskLevel.forValue(RiskLevelConstants.LOW_LEVEL_RISK), RiskLevel.LOW_LEVEL_RISK)
        assertEquals(RiskLevel.forValue(RiskLevelConstants.INCREASED_RISK), RiskLevel.INCREASED_RISK)
        assertEquals(
            RiskLevel.forValue(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS),
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
        )

        assertNotEquals(RiskLevel.forValue(RiskLevelConstants.UNKNOWN_RISK_INITIAL), RiskLevel.UNDETERMINED)
        assertNotEquals(
            RiskLevel.forValue(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF),
            RiskLevel.UNDETERMINED
        )
        assertNotEquals(RiskLevel.forValue(RiskLevelConstants.LOW_LEVEL_RISK), RiskLevel.UNDETERMINED)
        assertNotEquals(RiskLevel.forValue(RiskLevelConstants.INCREASED_RISK), RiskLevel.UNDETERMINED)
        assertNotEquals(RiskLevel.forValue(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS), RiskLevel.UNDETERMINED)
    }

    @Test
    fun testUnsuccessfulRistLevels() {
        assertTrue(RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(RiskLevel.UNDETERMINED))
        assertTrue(RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF))
        assertTrue(RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS))

        assertFalse(RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(RiskLevel.UNKNOWN_RISK_INITIAL))
        assertFalse(RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(RiskLevel.LOW_LEVEL_RISK))
        assertFalse(RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(RiskLevel.INCREASED_RISK))
    }

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
