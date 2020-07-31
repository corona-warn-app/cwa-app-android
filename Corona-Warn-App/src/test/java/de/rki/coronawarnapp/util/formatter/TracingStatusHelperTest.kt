package de.rki.coronawarnapp.util.formatter

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TracingStatusHelperTest {

    @Test
    fun testTracingActiveAllOn() {
        val result = tracingStatusHelper(tracing = true, bluetooth = true, connection = true, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_ACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenAllOff() {
        val result = tracingStatusHelper(tracing = false, bluetooth = false, connection = false, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenTracingOffBluetoothOffConnectionOn() {
        val result = tracingStatusHelper(tracing = false, bluetooth = false, connection = true, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenTracingOffBluetoothOnConnectionOff() {
        val result = tracingStatusHelper(tracing = false, bluetooth = true, connection = false, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testTracingInactiveWhenTracingOffBluetoothOnConnectionOn() {
        val result = tracingStatusHelper(tracing = false, bluetooth = true, connection = true, location = true)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testBluetoothInactiveWhenTracingOnBluetoothOffConnectionOn() {
        val result = tracingStatusHelper(tracing = true, bluetooth = false, connection = true, location = true)
        assertThat(result, `is`((TracingStatusHelper.BLUETOOTH)))
    }

    @Test
    fun testConnectionInactiveWhenTracingOnBluetoothOffConnectionOff() {
        val result = tracingStatusHelper(tracing = true, bluetooth = true, connection = false, location = true)
        assertThat(result, `is`((TracingStatusHelper.CONNECTION)))
    }

    @Test
    fun testConnectionInactiveWhenTracingOffBluetoothOnConnectionOnLocationOff() {
        val result = tracingStatusHelper(tracing = false, bluetooth = true, connection = true, location = false)
        assertThat(result, `is`((TracingStatusHelper.TRACING_INACTIVE)))
    }

    @Test
    fun testConnectionInactiveWhenTracingOnBluetoothOnConnectionOnLocationOff() {
        val result = tracingStatusHelper(tracing = true, bluetooth = true, connection = true, location = false)
        assertThat(result, `is`((TracingStatusHelper.LOCATION)))
    }
}
