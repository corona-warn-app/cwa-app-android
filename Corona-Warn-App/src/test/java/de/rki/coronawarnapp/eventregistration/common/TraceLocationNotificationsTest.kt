package de.rki.coronawarnapp.eventregistration.common

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.util.ApiLevel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceLocationNotificationsTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var apiLevel: ApiLevel
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat
    @MockK lateinit var notificationManager: NotificationManager

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    fun createInstance() = TraceLocationNotifications(
        context = context,
        apiLevel = apiLevel,
        notificationManagerCompat = notificationManagerCompat,
        notificationManager = notificationManager
    )

    @Test
    fun `basic case`() {
        val instance = createInstance()
        // TODO
    }
}
