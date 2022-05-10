package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.common.PresenceTracingNotifications
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentViewModel
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.observeForTesting

@ExtendWith(InstantExecutorExtension::class)
class NotificationSettingsFragmentViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK(relaxUnitFun = true) lateinit var notificationSettings: NotificationSettings
    @MockK lateinit var generalNotifications: GeneralNotifications
    @MockK lateinit var presenceTracingNotifications: PresenceTracingNotifications
    @MockK lateinit var digitalCovidCertificateNotifications: DigitalCovidCertificateNotifications

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { notificationSettings.isNotificationsEnabled } returns flow { emit(true) }
    }

    private fun createInstance(): NotificationSettingsFragmentViewModel =
        NotificationSettingsFragmentViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            notificationSettings = notificationSettings,
            generalNotifications = generalNotifications,
            presenceTracingNotifications = presenceTracingNotifications,
            digitalCovidCertificateNotifications = digitalCovidCertificateNotifications,
        )

    @Test
    fun `notification state`() {
        createInstance().apply {
            notificationSettingsState.observeForTesting { }
            notificationSettingsState.value shouldBe NotificationSettingsState(
                isNotificationsEnabled = true
            )
        }

        every { notificationSettings.isNotificationsEnabled } returns flow { emit(false) }
        createInstance().apply {
            notificationSettingsState.observeForTesting { }
            notificationSettingsState.value shouldBe NotificationSettingsState(
                isNotificationsEnabled = false
            )
        }
    }
}
