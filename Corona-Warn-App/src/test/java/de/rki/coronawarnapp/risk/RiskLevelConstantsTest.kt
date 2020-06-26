package de.rki.coronawarnapp.risk

import org.junit.Assert
import org.junit.Test

class RiskLevelConstantsTest {

    @Test
    fun allRiskLevelConstants() {
        Assert.assertEquals(RiskLevelConstants.UNKNOWN_RISK_INITIAL, 0)
        Assert.assertEquals(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF, 1)
        Assert.assertEquals(RiskLevelConstants.LOW_LEVEL_RISK, 2)
        Assert.assertEquals(RiskLevelConstants.INCREASED_RISK, 3)
        Assert.assertEquals(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS, 4)
        Assert.assertEquals(RiskLevelConstants.UNDETERMINED, 9001)
    }
}
