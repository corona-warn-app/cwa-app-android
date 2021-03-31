package de.rki.coronawarnapp.presencetracing.common

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.util.ApiLevel
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceLocationNotificationsTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var apiLevel: ApiLevel
    @MockK lateinit var notificationManager: NotificationManagerCompat

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        context.apply {
            every { packageName } returns "packagename"
            every { getString(any()) } returns ""
        }

        every { apiLevel.hasAPILevel(any()) } returns true

        notificationManager.apply {
            every { createNotificationChannel(any()) } just Runs
            every { notify(any(), any()) } just Runs
        }
    }

    fun createInstance() = TraceLocationNotifications(
        context = context,
        apiLevel = apiLevel,
        notificationManagerCompat = notificationManager
    )

    @Test
    fun `sending sets up a notification channel on API 26+`() {
        val instance = createInstance()
        instance.sendNotification(1, mockk())

        verify {
            notificationManager.createNotificationChannel(any())
            notificationManager.notify(1, any())
        }
    }

    @Test
    fun `sending does not setup a notification channel on API levels`() {
        every { apiLevel.hasAPILevel(any()) } returns true
        val instance = createInstance()
        instance.sendNotification(1, mockk())

        verify {
            notificationManager.createNotificationChannel(any())
            notificationManager.notify(1, any())
        }
    }
}
