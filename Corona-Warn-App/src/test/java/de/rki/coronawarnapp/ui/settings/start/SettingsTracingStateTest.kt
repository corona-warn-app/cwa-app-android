package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SettingsTracingStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `state mapping`() {
        TODO()
    }

    @Test
    fun `bluetooth disabled`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `location disabled`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `tracing inactive`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `tracing active`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

//     private fun formatTracingStatusBase(
//        bTracing: Boolean,
//        bBluetooth: Boolean,
//        bLocation: Boolean,
//        iValue: Int
//    ) {
//        every { context.getString(R.string.settings_tracing_status_restricted) } returns R.string.settings_tracing_status_restricted.toString()
//        every { context.getString(R.string.settings_tracing_status_active) } returns R.string.settings_tracing_status_active.toString()
//        every { context.getString(R.string.settings_tracing_status_inactive) } returns R.string.settings_tracing_status_inactive.toString()
//
//        val result = formatTracingStatusText(
//            tracing = bTracing,
//            bluetooth = bBluetooth,
//            location = bLocation
//        )
//        assertThat(result, `is`((context.getString(iValue))))
//    }
//    private fun formatTracingSwitchBase(
//        bTracing: Boolean,
//        bBluetooth: Boolean,
//        bLocation: Boolean,
//        bValue: Boolean
//    ) {
//        val result = formatTracingSwitch(
//            tracing = bTracing,
//            bluetooth = bBluetooth,
//            location = bLocation
//        )
//        assertThat(
//            result, `is`(bValue)
//        )
//    }
//
//    private fun formatTracingSwitchEnabledBase(
//        bTracing: Boolean,
//        bBluetooth: Boolean,
//        bLocation: Boolean,
//        bValue: Boolean
//    ) {
//        val result = formatTracingSwitchEnabled(
//            tracing = bTracing,
//            bluetooth = bBluetooth,
//            location = bLocation
//        )
//        assertThat(
//            result, `is`(bValue)
//        )
//    }
//
//    private fun formatTracingStatusImageBase(
//        bTracing: Boolean,
//        bBluetooth: Boolean,
//        bLocation: Boolean
//    ) {
//        every { context.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off) } returns drawable
//        every { context.getDrawable(R.drawable.ic_illustration_tracing_on) } returns drawable
//        every { context.getDrawable(R.drawable.ic_settings_illustration_tracing_off) } returns drawable
//
//        val result = formatTracingStatusImage(
//            tracing = bTracing,
//            bluetooth = bBluetooth,
//            location = bLocation
//        )
//        assertThat(
//            result, `is`(CoreMatchers.equalTo(drawable))
//        )
//    }
//
//    private fun formatTracingStatusVisibilityTracingBase(
//        bTracing: Boolean,
//        bBluetooth: Boolean,
//        bLocation: Boolean
//    ) {
//        val result =
//            formatTracingStatusVisibilityTracing(
//                tracing = bTracing,
//                bluetooth = bBluetooth,
//                location = bLocation
//            )
//        assertThat(true, `is`(result > -1))
//    }
//
//    private fun formatTracingStatusVisibilityBluetoothBase(
//        bTracing: Boolean,
//        bBluetooth: Boolean,
//        bLocation: Boolean
//    ) {
//        val result =
//            formatTracingStatusVisibilityBluetooth(
//                tracing = bTracing,
//                bluetooth = bBluetooth,
//                location = bLocation
//            )
//        assertThat(true, `is`(result > -1))
//    }
//     @Test
//    fun formatTracingStatusText() {
//        // When tracing is true, bluetooth is true, location is true
//        formatTracingStatusBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true,
//            iValue = R.string.settings_tracing_status_active
//        )
//
//        // When tracing is false, bluetooth is false, location is false
//        formatTracingStatusBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = false,
//            iValue = R.string.settings_tracing_status_inactive
//        )
//
//        // When tracing is true, bluetooth is false, location is true
//        formatTracingStatusBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true,
//            iValue = R.string.settings_tracing_status_restricted
//        )
//
//        // When tracing is true, bluetooth is true, location is true
//        formatTracingStatusBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true,
//            iValue = R.string.settings_tracing_status_active
//        )
//
//        // When tracing is false, bluetooth is true, location is false
//        formatTracingStatusBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = false,
//            iValue = R.string.settings_tracing_status_inactive
//        )
//
//        // When tracing is false, bluetooth is true, location is true
//        formatTracingStatusBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true,
//            iValue = R.string.settings_tracing_status_inactive
//        )
//
//        // When tracing is true, bluetooth is false, location is true
//        formatTracingStatusBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true,
//            iValue = R.string.settings_tracing_status_restricted
//        )
//
//        // When tracing is false, bluetooth is false, location is true
//        formatTracingStatusBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true,
//            iValue = R.string.settings_tracing_status_inactive
//        )
//    }
//     @Test
//    fun formatTracingSwitch() {
//        formatTracingSwitchBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = false,
//            bValue = false
//        )
//
//        formatTracingSwitchBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = false,
//            bValue = false
//        )
//    }
//     @Test
//    fun formatTracingSwitchEnabled() {
//
//        formatTracingSwitchEnabledBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = true
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true,
//            bValue = false
//        )
//
//        formatTracingSwitchEnabledBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true,
//            bValue = true
//        )
//    }
//
//    @Test
//    fun formatTracingStatusImage() {
//        formatTracingStatusImageBase(bTracing = true, bBluetooth = true, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = false, bBluetooth = false, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = false, bBluetooth = false, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = false, bBluetooth = true, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = false, bBluetooth = true, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = true, bBluetooth = false, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = true, bBluetooth = false, bLocation = true)
//
//        formatTracingStatusImageBase(bTracing = true, bBluetooth = true, bLocation = true)
//    }
//
//    @Test
//    fun formatTracingStatusVisibilityBluetooth() {
//        formatTracingStatusVisibilityBluetoothBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityBluetoothBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityBluetoothBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityBluetoothBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityBluetoothBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityBluetoothBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true
//        )
//    }
//
//    @Test
//    fun formatTracingStatusVisibilityTracing() {
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = false,
//            bBluetooth = false,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = false,
//            bBluetooth = true,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = true,
//            bBluetooth = false,
//            bLocation = true
//        )
//
//        formatTracingStatusVisibilityTracingBase(
//            bTracing = true,
//            bBluetooth = true,
//            bLocation = true
//        )
//    }
}
