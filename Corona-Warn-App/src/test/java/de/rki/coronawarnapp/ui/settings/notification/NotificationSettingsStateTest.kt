package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsState
import de.rki.coronawarnapp.util.ContextExtensions
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class NotificationSettingsStateTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(ContextExtensions)
        with(context) {
            every { getString(any()) } returns ""
            every { getColor(any()) } returns 0
            every { getDrawableCompat(any()) } returns mockk()
        }
    }

    private fun createInstance(
        isNotificationsEnabled: Boolean = true
    ) = NotificationSettingsState(
        isNotificationsEnabled = isNotificationsEnabled
    )

    @Test
    fun getNotificationsHeader() {
        createInstance().getNotificationsHeader() shouldBe R.string.nm_notification_settings
        createInstance(false).getNotificationsHeader() shouldBe R.string.nm_notification_enabled
    }

    @Test
    fun getNotificationsImage() {
        createInstance().getNotificationsImage() shouldBe R.drawable.ic_illustration_notification_on
        createInstance(false).getNotificationsImage() shouldBe R.drawable.ic_settings_illustration_notification_off
    }

    @Test
    fun getNotificationsIllustrationText() {
        createInstance().getNotificationsIllustrationText(context)
        verify(exactly = 1) { context.getString(R.string.settings_notifications_illustration_description_active) }

        createInstance(isNotificationsEnabled = false).getNotificationsIllustrationText(context)
        verify(exactly = 1) { context.getString(R.string.settings_notifications_illustration_description_inactive) }
    }

    @Test
    fun getTestNotificationStatusText() {
        createInstance().getNotificationStatusText() shouldBe R.string.settings_on
        createInstance(false).getNotificationStatusText() shouldBe R.string.settings_off
    }
}
