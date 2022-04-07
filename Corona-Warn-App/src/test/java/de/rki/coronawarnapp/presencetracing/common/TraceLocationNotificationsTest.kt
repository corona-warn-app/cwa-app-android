package de.rki.coronawarnapp.presencetracing.common

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceLocationNotificationsTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var notificationManager: NotificationManagerCompat
    @MockK lateinit var deepLinkBuilderFactory: NavDeepLinkBuilderFactory

    private val channelSlot = slot<NotificationChannelCompat>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        context.apply {
            every { packageName } returns "packagename"
            every { getString(any()) } returns ""
        }

        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_INT } returns 42

        notificationManager.apply {
            every { createNotificationChannel(capture(channelSlot)) } just Runs
            every { notify(any(), any()) } just Runs
        }
    }

    fun createInstance() = PresenceTracingNotifications(
        context = context,
        notificationManagerCompat = notificationManager,
        navDeepLinkBuilderFactory = deepLinkBuilderFactory
    )

    @Test
    fun `sending sets up a notification channel on API 26+`() {
        val instance = createInstance()
        instance.sendNotification(1, mockk())

        channelSlot.captured.id shouldBe "packagename.notification.presenceTracingChannelId"

        verify {
            notificationManager.createNotificationChannel(any<NotificationChannelCompat>())
            notificationManager.notify(1, any())
        }
    }

    @Test
    fun `sending does not setup a notification channel on sub API26`() {
        every { BuildVersionWrap.SDK_INT } returns 21
        val instance = createInstance()
        instance.sendNotification(1, mockk())

        verify(exactly = 0) { notificationManager.createNotificationChannel(any<NotificationChannelCompat>()) }
        verify { notificationManager.notify(1, any()) }
    }
}
