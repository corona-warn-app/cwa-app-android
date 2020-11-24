package de.rki.coronawarnapp.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
}
