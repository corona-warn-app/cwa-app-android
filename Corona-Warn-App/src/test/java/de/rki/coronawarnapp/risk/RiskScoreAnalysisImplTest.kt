package de.rki.coronawarnapp.risk

import org.junit.Assert
import org.junit.Test

class RiskScoreAnalysisImplTest {

    @Test
    fun test_withinDefinedLevelThreshold() {
        val instance = RiskScoreAnalysisImpl()

        // positive
        Assert.assertTrue(instance.withinDefinedLevelThreshold(2.0, 1, 3))

        // negative
        Assert.assertFalse(instance.withinDefinedLevelThreshold(4.0, 1, 3))

        // edge cases
        Assert.assertTrue(instance.withinDefinedLevelThreshold(1.0, 1, 3))
        Assert.assertTrue(instance.withinDefinedLevelThreshold(3.0, 1, 3))
    }
}
