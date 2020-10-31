package de.rki.coronawarnapp.deadman

import android.content.Context
import de.rki.coronawarnapp.notification.NotificationConstants
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationSenderTest : BaseTest() {

    @MockK lateinit var context: Context

    private val channelId = "de.rki.coronawarnapp.notification.exposureNotificationChannelId"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.getString(NotificationConstants.NOTIFICATION_CHANNEL_ID)} returns channelId
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createSender() = DeadmanNotificationSender(
        context = context
    )

    @Test
    fun `sender creation`()  {
        createSender()
    }
}
