package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SettingsBackgroundStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(isEnabled: Boolean) = SettingsBackgroundState(
        isEnabled = isEnabled
    )

    @Test
    fun getBackgroundPriorityIconColor() {
        createInstance(isEnabled = true).apply {
            getBackgroundPriorityIconColor(context)
            verify { context.getColorCompat(R.color.colorAccentTintIcon) }
        }
        createInstance(isEnabled = false).apply {
            getBackgroundPriorityIconColor(context)
            verify { context.getColorCompat(R.color.colorTextSemanticRed) }
        }
    }

    @Test
    fun getBackgroindPriorityIcon() {
        createInstance(isEnabled = true).apply {
            getBackgroundPriorityIcon(context)
            verify { context.getDrawableCompat(R.drawable.ic_settings_background_priority_enabled) }
        }
        createInstance(isEnabled = false).apply {
            getBackgroundPriorityIcon(context)
            verify { context.getDrawableCompat(R.drawable.ic_settings_background_priority_disabled) }
        }
    }

    @Test
    fun getBackgroundPriorityText() {
        createInstance(isEnabled = true).apply {
            getBackgroundPriorityText(context)
            verify { context.getString(R.string.settings_on) }
        }
        createInstance(isEnabled = false).apply {
            getBackgroundPriorityText(context)
            verify { context.getString(R.string.settings_off) }
        }
    }
}
