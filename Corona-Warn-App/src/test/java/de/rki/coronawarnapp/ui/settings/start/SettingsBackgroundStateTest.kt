package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import de.rki.coronawarnapp.R
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
            verify { context.getColor(R.color.colorAccentTintIcon) }
        }
        createInstance(isEnabled = false).apply {
            getBackgroundPriorityIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticRed) }
        }
    }

    @Test
    fun getBackgroindPriorityIcon() {
        createInstance(isEnabled = true).apply {
            getBackgroindPriorityIcon(context)
            verify { context.getDrawable(R.drawable.ic_settings_background_priority_enabled) }
        }
        createInstance(isEnabled = false).apply {
            getBackgroindPriorityIcon(context)
            verify { context.getDrawable(R.drawable.ic_settings_background_priority_disabled) }
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
