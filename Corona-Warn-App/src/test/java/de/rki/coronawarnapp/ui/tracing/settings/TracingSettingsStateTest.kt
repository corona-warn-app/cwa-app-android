package de.rki.coronawarnapp.ui.tracing.settings

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.verify
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
        TracingSettingsState.BluetoothDisabled.apply {
            getTracingStatusImage(context)
            verify { context.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off) }

            getTracingIllustrationText(context)
            verify { context.getString(R.string.settings_tracing_bluetooth_illustration_description_inactive) }

            isTracingSwitchEnabled() shouldBe false
            isTracingSwitchChecked() shouldBe false

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_restricted) }

            isLocationCardVisible() shouldBe false
            isBluetoothCardVisible() shouldBe true
            isTracingStatusTextVisible() shouldBe false
        }
    }

    @Test
    fun `location disabled`() {
        TracingSettingsState.LocationDisabled.apply {
            getTracingStatusImage(context)
            verify { context.getDrawable(R.drawable.ic_settings_illustration_location_off) }

            getTracingIllustrationText(context)
            verify { context.getString(R.string.settings_tracing_location_illustration_description_inactive) }

            isTracingSwitchEnabled() shouldBe false
            isTracingSwitchChecked() shouldBe false

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_inactive) }

            isLocationCardVisible() shouldBe true
            isBluetoothCardVisible() shouldBe false
            isTracingStatusTextVisible() shouldBe false
        }
    }

    @Test
    fun `tracing inactive`() {
        TracingSettingsState.TracingInActive.apply {
            getTracingStatusImage(context)
            verify { context.getDrawable(R.drawable.ic_settings_illustration_tracing_off) }

            getTracingIllustrationText(context)
            verify { context.getString(R.string.settings_tracing_illustration_description_inactive) }

            isTracingSwitchEnabled() shouldBe true
            isTracingSwitchChecked() shouldBe false

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_inactive) }

            isLocationCardVisible() shouldBe false
            isBluetoothCardVisible() shouldBe false
            isTracingStatusTextVisible() shouldBe true
        }
    }

    @Test
    fun `tracing active`() {
        TracingSettingsState.TracingActive.apply {
            getTracingStatusImage(context)
            verify { context.getDrawable(R.drawable.ic_illustration_tracing_on) }

            getTracingIllustrationText(context)
            verify { context.getString(R.string.settings_tracing_illustration_description_active) }

            isTracingSwitchEnabled() shouldBe true
            isTracingSwitchChecked() shouldBe true

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_active) }

            isLocationCardVisible() shouldBe false
            isBluetoothCardVisible() shouldBe false
            isTracingStatusTextVisible() shouldBe true
        }
    }
}
