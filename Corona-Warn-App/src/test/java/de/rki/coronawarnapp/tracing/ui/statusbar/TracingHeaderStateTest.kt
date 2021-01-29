package de.rki.coronawarnapp.tracing.ui.statusbar

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState.BluetoothDisabled
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState.LocationDisabled
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState.TracingActive
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState.TracingInActive
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TracingHeaderStateTest : BaseTest() {

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
        Status.TRACING_ACTIVE.toHeaderState() shouldBe TracingActive
        Status.TRACING_INACTIVE.toHeaderState() shouldBe TracingInActive
        Status.LOCATION_DISABLED.toHeaderState() shouldBe LocationDisabled
        Status.BLUETOOTH_DISABLED.toHeaderState() shouldBe BluetoothDisabled
    }

    @Test
    fun `bluetooth disabled`() {
        BluetoothDisabled.apply {
            getTracingAnimation(context) shouldBe R.drawable.ic_settings_tracing_bluetooth_inactive

            getTracingTint(context)
            verify { context.getColorCompat(R.color.colorTextSemanticRed) }

            getTracingDescription(context)
            verify { context.getString(R.string.settings_tracing_body_bluetooth_inactive) }

            getTracingContentDescription(context)
            verify {
                context.getString(R.string.settings_tracing_body_bluetooth_inactive)
                context.getString(R.string.accessibility_button)
            }
        }
    }

    @Test
    fun `location disabled`() {
        LocationDisabled.apply {
            getTracingAnimation(context) shouldBe R.drawable.ic_settings_location_inactive_small

            getTracingTint(context)
            verify { context.getColorCompat(R.color.colorTextSemanticRed) }

            getTracingDescription(context)
            verify { context.getString(R.string.settings_tracing_body_inactive_location) }

            getTracingContentDescription(context)
            verify {
                context.getString(R.string.settings_tracing_body_inactive_location)
                context.getString(R.string.accessibility_button)
            }
        }
    }

    @Test
    fun `tracing inactive`() {
        TracingInActive.apply {
            getTracingAnimation(context) shouldBe R.drawable.ic_settings_tracing_inactive

            getTracingTint(context)
            verify { context.getColorCompat(R.color.colorTextSemanticRed) }

            getTracingDescription(context)
            verify { context.getString(R.string.settings_tracing_body_inactive) }

            getTracingContentDescription(context)
            verify {
                context.getString(R.string.settings_tracing_body_inactive)
                context.getString(R.string.accessibility_button)
            }
        }
    }

    @Test
    fun `tracing active`() {
        TracingActive.apply {
            getTracingAnimation(context) shouldBe R.raw.ic_settings_tracing_animated

            getTracingTint(context)
            verify { context.getColorCompat(R.color.colorAccentTintIcon) }

            getTracingDescription(context)
            verify { context.getString(R.string.settings_tracing_body_active) }

            getTracingContentDescription(context)
            verify {
                context.getString(R.string.settings_tracing_body_active)
                context.getString(R.string.accessibility_button)
            }
        }
    }
}
