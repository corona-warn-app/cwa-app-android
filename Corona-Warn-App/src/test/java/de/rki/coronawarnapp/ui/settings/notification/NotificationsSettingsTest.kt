package de.rki.coronawarnapp.ui.settings.notification

import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class NotificationsSettingsTest : BaseTest() {

    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var notificationManagerCompat: NotificationManagerCompat

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(LocalData)

        every { LocalData.isNotificationsRiskEnabledFlow } returns flow { emit(true) }
        every { LocalData.isNotificationsRiskEnabled = any() } just Runs
        every { LocalData.isNotificationsRiskEnabled } returns true

        every { LocalData.isNotificationsTestEnabledFlow } returns flow { emit(true) }
        every { LocalData.isNotificationsTestEnabled = any() } just Runs
        every { LocalData.isNotificationsTestEnabled } returns true

        every { notificationManagerCompat.areNotificationsEnabled() } returns true
        coEvery { foregroundState.isInForeground } returns flow { emit(true) }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = NotificationSettings(
        foregroundState = foregroundState,
        notificationManagerCompat = notificationManagerCompat
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

    @Test
    fun toggleNotificationsRiskEnabled() {
        createInstance().toggleNotificationsRiskEnabled()
        verify { LocalData.isNotificationsRiskEnabled = false }
    }

    @Test
    fun toggleNotificationsTestEnabled() {
        createInstance().toggleNotificationsTestEnabled()
        verify { LocalData.isNotificationsTestEnabled = false }
    }
}
