package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SettingsNotificationStateTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(ContextExtensions)
        every { context.getString(any()) } returns ""
        every { context.getColorCompat(any()) } returns 0
        every { context.getDrawableCompat(any()) } returns mockk()
    }

    @Test
    fun getNotificationIconColor() = runTest {
        SettingsNotificationState(
            isNotificationsEnabled = true,
        ).getNotificationIconColor(context)
        verify { context.getColorCompat(R.color.colorAccentTintIcon) }

        SettingsNotificationState(
            isNotificationsEnabled = false,
        ).getNotificationIconColor(context)
        verify { context.getColorCompat(R.color.colorTextSemanticRed) }
    }

    @Test
    fun getNotificationIcon() = runTest {
        SettingsNotificationState(
            isNotificationsEnabled = true,
        ).getNotificationIcon(context)
        verify { context.getDrawableCompat(R.drawable.ic_settings_notification_active) }

        SettingsNotificationState(
            isNotificationsEnabled = false,
        ).getNotificationIcon(context)
        verify {
            context.getDrawableCompat(
                R.drawable.ic_settings_notification_inactive
            )
        }
    }

    @Test
    fun getNotificationStatusText() {
        SettingsNotificationState(
            isNotificationsEnabled = true,
        ).getNotificationStatusText(context)
        verify { context.getString(R.string.settings_on) }

        SettingsNotificationState(
            isNotificationsEnabled = false,
        ).getNotificationStatusText(context)
        verify { context.getString(R.string.settings_off) }
    }
}
