package de.rki.coronawarnapp.eventregistration.checkins.checkout

import android.content.Context
import de.rki.coronawarnapp.eventregistration.common.TraceLocationNotifications
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AutoCheckoutNotificationTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var traceLocationNotifications: TraceLocationNotifications

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { foregroundState.isInForeground } returns flowOf(false)
    }

    private fun createInstance() = CheckOutNotification(
        notificationHelper = traceLocationNotifications,
        context = context,
        foregroundState = foregroundState,
    )

    @Test
    fun `notification is created`() = runBlockingTest {
        val instance = createInstance()
        instance.showAutoCheckoutNotification(42L)
        verify { traceLocationNotifications.sendNotification(any(), any()) }
    }
}
