package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentViewModel
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.observeForTesting

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class NotificationSettingsFragmentViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK(relaxUnitFun = true) lateinit var notificationSettings: NotificationSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { notificationSettings.isNotificationsEnabled } returns flow { emit(true) }
        every { notificationSettings.isNotificationsRiskEnabled } returns flow { emit(false) }
        every { notificationSettings.isNotificationsTestEnabled } returns flow { emit(true) }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): NotificationSettingsFragmentViewModel =
        NotificationSettingsFragmentViewModel(
            dispatcherProvider = TestDispatcherProvider,
            notificationSettings = notificationSettings
        )

    @Test
    fun `notification state`() {
        createInstance().apply {
            notificationSettingsState.observeForTesting { }
            notificationSettingsState.value shouldBe NotificationSettingsState(
                isNotificationsEnabled = true,
                isNotificationsRiskEnabled = false,
                isNotificationsTestEnabled = true
            )
        }

        every { notificationSettings.isNotificationsEnabled } returns flow { emit(false) }
        every { notificationSettings.isNotificationsRiskEnabled } returns flow { emit(true) }
        every { notificationSettings.isNotificationsTestEnabled } returns flow { emit(false) }
        createInstance().apply {
            notificationSettingsState.observeForTesting { }
            notificationSettingsState.value shouldBe NotificationSettingsState(
                isNotificationsEnabled = false,
                isNotificationsRiskEnabled = true,
                isNotificationsTestEnabled = false
            )
        }
    }

    @Test
    fun `toggle risk notifications`() {
        createInstance().toggleNotificationsRiskEnabled()
        verify { notificationSettings.toggleNotificationsRiskEnabled() }
    }

    @Test
    fun `toggle test notifications`() {
        createInstance().toggleNotificationsTestEnabled()
        verify { notificationSettings.toggleNotificationsTestEnabled() }
    }
}
