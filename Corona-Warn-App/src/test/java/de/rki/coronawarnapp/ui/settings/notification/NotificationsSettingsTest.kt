package de.rki.coronawarnapp.ui.settings.notification

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verifySequence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class NotificationsSettingsTest : BaseTest() {

    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat
    @MockK lateinit var cwaSettings: CWASettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { cwaSettings.isNotificationsRiskEnabled } returns mockFlowPreference(true)
        every { cwaSettings.isNotificationsTestEnabled } returns mockFlowPreference(true)
        every { notificationManagerCompat.areNotificationsEnabled() } returns true
        coEvery { foregroundState.isInForeground } returns flow { emit(true) }
    }

    private fun createInstance() = NotificationSettings(
        foregroundState = foregroundState,
        notificationManagerCompat = notificationManagerCompat,
        cwaSettings = cwaSettings
    )

    @Test
    fun isNotificationsEnabled() = runBlockingTest {
        createInstance().apply {
            isNotificationsEnabled.first() shouldBe true
            verifySequence {
                foregroundState.isInForeground
                notificationManagerCompat.areNotificationsEnabled()
            }
        }
    }

    @Test
    fun isNotificationsRiskEnabled() = runBlockingTest {
        createInstance().apply {
            isNotificationsRiskEnabled.first() shouldBe true
        }
    }

    @Test
    fun isNotificationsTestEnabled() = runBlockingTest {
        createInstance().apply {
            isNotificationsTestEnabled.first() shouldBe true
        }
    }
}
