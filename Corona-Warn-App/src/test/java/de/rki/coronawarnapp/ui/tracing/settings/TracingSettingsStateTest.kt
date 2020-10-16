package de.rki.coronawarnapp.ui.tracing.settings

import android.content.Context
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TracingSettingsStateTest : BaseTest() {

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
        GeneralTracingStatus.Status.TRACING_ACTIVE.toTracingSettingsState() shouldBe TracingSettingsState.TracingActive
        GeneralTracingStatus.Status.TRACING_INACTIVE.toTracingSettingsState() shouldBe TracingSettingsState.TracingInActive
        GeneralTracingStatus.Status.LOCATION_DISABLED.toTracingSettingsState() shouldBe TracingSettingsState.LocationDisabled
        GeneralTracingStatus.Status.BLUETOOTH_DISABLED.toTracingSettingsState() shouldBe TracingSettingsState.BluetoothDisabled
    }

    @Test
    fun `bluetooth disabled`() {
        TODO()
    }

    @Test
    fun `location disabled`() {
        TODO()
    }

    @Test
    fun `tracing inactive`() {
        TODO()
    }

    @Test
    fun `tracing active`() {
        TODO()
    }
}
