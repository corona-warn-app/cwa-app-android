package de.rki.coronawarnapp.util.formatter

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TracingStatusHelperTest {

    @Test
    fun testTracingActiveAllOn() {
        val result = tracingStatusHelper(tracing = true, bluetooth = true, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_ACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenAllOff() {
        val result = tracingStatusHelper(tracing = false, bluetooth = false, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenTracingOffBluetoothOff() {
        val result = tracingStatusHelper(tracing = false, bluetooth = false, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenTracingOffBluetoothOn() {
        val result = tracingStatusHelper(tracing = false, bluetooth = true, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testBluetoothInactiveWhenTracingOnBluetoothOff() {
        val result = tracingStatusHelper(tracing = true, bluetooth = false, location = true)
        assertThat(result, `is`((TracingStatusHelper.BLUETOOTH)))
    }

    @Test
    fun testConnectionInactiveWhenTracingOffBluetoothOnLocationOff() {
        val result = tracingStatusHelper(tracing = false, bluetooth = true, location = false)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testConnectionInactiveWhenTracingOnBluetoothOnLocationOff() {
        val result = tracingStatusHelper(tracing = true, bluetooth = true, location = false)
        assertThat(result, `is`((TracingStatusHelper.LOCATION)))
    }
}
