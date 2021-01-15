package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
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

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `enabled state`() {
        SettingsNotificationState(
            isNotificationsEnabled = true,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).isEnabled shouldBe true

        SettingsNotificationState(
            isNotificationsEnabled = false,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).isEnabled shouldBe false

        SettingsNotificationState(
            isNotificationsEnabled = true,
            isNotificationsRiskEnabled = false,
            isNotificationsTestEnabled = false
        ).isEnabled shouldBe false

        SettingsNotificationState(
            isNotificationsEnabled = true,
            isNotificationsRiskEnabled = false,
            isNotificationsTestEnabled = true
        ).isEnabled shouldBe true

        SettingsNotificationState(
            isNotificationsEnabled = true,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = false
        ).isEnabled shouldBe true
    }

    @Test
    fun getNotificationIconColor() = runBlockingTest {
        SettingsNotificationState(
            isNotificationsEnabled = true,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).getNotificationIconColor(context)
        verify { context.getColorCompat(R.color.colorAccentTintIcon) }

        SettingsNotificationState(
            isNotificationsEnabled = false,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).getNotificationIconColor(context)
        verify { context.getColorCompat(R.color.colorTextSemanticRed) }
    }

    @Test
    fun getNotificationIcon() = runBlockingTest {
        SettingsNotificationState(
            isNotificationsEnabled = true,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).getNotificationIcon(context)
        verify { context.getDrawableCompat(R.drawable.ic_settings_notification_active) }

        SettingsNotificationState(
            isNotificationsEnabled = false,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
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
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).getNotificationStatusText(context)
        verify { context.getString(R.string.settings_on) }

        SettingsNotificationState(
            isNotificationsEnabled = false,
            isNotificationsRiskEnabled = true,
            isNotificationsTestEnabled = true
        ).getNotificationStatusText(context)
        verify { context.getString(R.string.settings_off) }
    }
}
