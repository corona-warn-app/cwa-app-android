package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Test

class TimeVariablesTest {

    @Test
    fun getDeactivationTracingMeasureThresholdTimeRange() {
        Assert.assertEquals(TimeVariables.getDeactivationTracingMeasureThresholdTimeRange(), 60L)
    }

    @Test
    fun getTransactionTimeout() {
        Assert.assertEquals(TimeVariables.getTransactionTimeout(), 480000L)
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
    fun getManualKeyRetrievalDelay() {
        mockkObject(CWADebug)
        every { CWADebug.buildFlavor } returns CWADebug.BuildFlavor.DEVICE_FOR_TESTERS
        Assert.assertEquals(TimeVariables.getManualKeyRetrievalDelay(), 1000 * 60)

        every { CWADebug.buildFlavor } returns CWADebug.BuildFlavor.DEVICE
        Assert.assertEquals(TimeVariables.getManualKeyRetrievalDelay(), 1000 * 60 * 60 * 24)
    }

    @Test
    fun getMaxAttenuationDuration() {
        Assert.assertEquals(TimeVariables.getMaxAttenuationDuration(), 30)
    }
}
