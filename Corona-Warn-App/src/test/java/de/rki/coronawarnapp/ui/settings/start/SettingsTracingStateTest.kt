package de.rki.coronawarnapp.ui.settings.start

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
        GeneralTracingStatus.Status.TRACING_ACTIVE.toSettingsTracingState() shouldBe SettingsTracingState.TracingActive
        GeneralTracingStatus.Status.TRACING_INACTIVE.toSettingsTracingState() shouldBe SettingsTracingState.TracingInActive
        GeneralTracingStatus.Status.LOCATION_DISABLED.toSettingsTracingState() shouldBe SettingsTracingState.LocationDisabled
        GeneralTracingStatus.Status.BLUETOOTH_DISABLED.toSettingsTracingState() shouldBe SettingsTracingState.BluetoothDisabled
    }

    @Test
    fun `bluetooth disabled`() {
        SettingsTracingState.BluetoothDisabled.apply {
            getTracingIconColor(context)
            verify { context.getColor(R.color.colorTextPrimary3) }

            getTracingIcon(context)
            verify { context.getDrawable(R.drawable.ic_settings_tracing_active_small) }

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_restricted) }
        }
    }

    @Test
    fun `location disabled`() {
        SettingsTracingState.LocationDisabled.apply {
            getTracingIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticRed) }

            getTracingIcon(context)
            verify { context.getDrawable(R.drawable.ic_settings_location_inactive_small) }

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_inactive) }
        }
    }

    @Test
    fun `tracing inactive`() {
        SettingsTracingState.TracingInActive.apply {
            getTracingIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticRed) }

            getTracingIcon(context)
            verify { context.getDrawable(R.drawable.ic_settings_tracing_inactive_small) }

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_inactive) }
        }
    }

    @Test
    fun `tracing active`() {
        SettingsTracingState.TracingActive.apply {
            getTracingIconColor(context)
            verify { context.getColor(R.color.colorAccentTintIcon) }

            getTracingIcon(context)
            verify { context.getDrawable(R.drawable.ic_settings_tracing_active_small) }

            getTracingStatusText(context)
            verify { context.getString(R.string.settings_tracing_status_active) }
        }
    }
}
