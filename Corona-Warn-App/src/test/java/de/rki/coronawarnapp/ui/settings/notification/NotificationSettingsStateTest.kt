package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsState
import de.rki.coronawarnapp.util.ContextExtensions
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
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

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(
        isNotificationsEnabled: Boolean = true,
        isNotificationsRiskEnabled: Boolean = true,
        isNotificationsTestEnabled: Boolean = true
    ) = NotificationSettingsState(
        isNotificationsEnabled = isNotificationsEnabled,
        isNotificationsRiskEnabled = isNotificationsRiskEnabled,
        isNotificationsTestEnabled = isNotificationsTestEnabled
    )

    @Test
    fun getNotificationsTitle() {
        createInstance().getNotificationsTitle(context)

        createInstance(isNotificationsEnabled = false).getNotificationsTitle(context)
        verify(exactly = 1) { context.getString(R.string.settings_notifications_headline_active) }
        verify(exactly = 1) { context.getString(any()) }
    }

    @Test
    fun getNotificationsDescription() {
        createInstance().getNotificationsDescription(context)

        createInstance(isNotificationsEnabled = false).getNotificationsDescription(context)
        verify(exactly = 1) { context.getString(R.string.settings_notifications_body_active) }
        verify(exactly = 1) { context.getString(any()) }
    }

    @Test
    fun getNotificationsImage() {
        createInstance().getNotificationsImage(context)
        verify(exactly = 1) { context.getDrawableCompat(R.drawable.ic_illustration_notification_on) }

        createInstance(isNotificationsEnabled = false).getNotificationsImage(context)
        verify(exactly = 1) { context.getDrawableCompat(R.drawable.ic_settings_illustration_notification_off) }
    }

    @Test
    fun getNotificationsIllustrationText() {
        createInstance().getNotificationsIllustrationText(context)
        verify(exactly = 1) { context.getString(R.string.settings_notifications_illustration_description_active) }

        createInstance(isNotificationsEnabled = false).getNotificationsIllustrationText(context)
        verify(exactly = 1) { context.getString(R.string.settings_notifications_illustration_description_inactive) }
    }

    @Test
    fun getRiskNotificationStatusText() {
        createInstance().getRiskNotificationStatusText(context)
        verify(exactly = 1) { context.getString(R.string.settings_on) }

        createInstance(isNotificationsRiskEnabled = false).getRiskNotificationStatusText(context)
        verify(exactly = 1) { context.getString(R.string.settings_off) }
    }

    @Test
    fun getTestNotificationStatusText() {
        createInstance().getTestNotificationStatusText(context)
        verify(exactly = 1) { context.getString(R.string.settings_on) }

        createInstance(isNotificationsTestEnabled = false).getTestNotificationStatusText(context)
        verify(exactly = 1) { context.getString(R.string.settings_off) }
    }

    @Test
    fun isNotificationCardVisible() {
        createInstance(isNotificationsEnabled = true).isNotificationCardVisible() shouldBe false
        createInstance(isNotificationsEnabled = false).isNotificationCardVisible() shouldBe true
    }
}
