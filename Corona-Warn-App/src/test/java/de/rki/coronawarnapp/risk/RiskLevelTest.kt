package de.rki.coronawarnapp.risk

import org.junit.Assert.assertEquals
import org.junit.Test

class RiskLevelTest {

    @Test
    fun testEnum() {
        assertEquals(
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF.raw,
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF
        )
        assertEquals(RiskLevel.LOW_LEVEL_RISK.raw, RiskLevelConstants.LOW_LEVEL_RISK)
        assertEquals(RiskLevel.INCREASED_RISK.raw, RiskLevelConstants.INCREASED_RISK)
        assertEquals(RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS.raw, RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        assertEquals(RiskLevel.UNDETERMINED.raw, RiskLevelConstants.UNDETERMINED)
    }
}
