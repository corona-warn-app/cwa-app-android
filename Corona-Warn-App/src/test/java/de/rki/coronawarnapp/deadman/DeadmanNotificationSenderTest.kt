package de.rki.coronawarnapp.deadman

import android.content.Context
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationSenderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var notificationHelper: NotificationHelper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createSender() = DeadmanNotificationSender(
        context = context,
        foregroundState = foregroundState,
        notificationHelper = notificationHelper
    )

    @Test
    fun `sender creation`() {
        shouldNotThrowAny {
            createSender()
        }
    }
}
