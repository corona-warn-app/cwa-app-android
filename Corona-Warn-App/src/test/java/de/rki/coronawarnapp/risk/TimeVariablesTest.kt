package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds
import org.junit.Assert
import org.junit.Test

class TimeVariablesTest {

    @Test
    fun getDeactivationTracingMeasureThresholdTimeRange() {
        Assert.assertEquals(TimeVariables.getDeactivationTracingMeasureThresholdTimeRange(), 60L)
    }

    @Test
    fun getTransactionTimeout() {
        Assert.assertEquals(TimeVariables.getTransactionTimeout(), 60000L)
    }

    @Test
    fun getDefaultRetentionPeriodInDays() {
        Assert.assertEquals(TimeVariables.getDefaultRetentionPeriodInDays(), 14)
    }

    @Test
    fun getDefaultRetentionPeriodInMS() {
        Assert.assertEquals(TimeVariables.getDefaultRetentionPeriodInMS(), 14.toLong().daysToMilliseconds())
    }

    @Test
    fun getMinActivatedTracingTime() {
        Assert.assertEquals(TimeVariables.getMinActivatedTracingTime(), 24)
    }

    @Test
    fun getMaxStaleExposureRiskRange() {
        Assert.assertEquals(TimeVariables.getMaxStaleExposureRiskRange(), 48)
    }

    @Test
    fun getManualKeyRetrievalDelay() {
        if (BuildConfig.FLAVOR == "deviceForTesters") {
            Assert.assertEquals(TimeVariables.getManualKeyRetrievalDelay(), 1000 * 60)
        } else {
            Assert.assertEquals(TimeVariables.getManualKeyRetrievalDelay(), 1000 * 60 * 60 * 24)
        }
    }

    @Test
    fun getMaxAttenuationDuration() {
        Assert.assertEquals(TimeVariables.getMaxAttenuationDuration(), 30)
    }
}
