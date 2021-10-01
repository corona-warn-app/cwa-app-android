package de.rki.coronawarnapp.ui.settings.notifications

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.common.PresenceTracingNotifications
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

class NotificationSettingsFragmentTest : BaseUITest() {

    @MockK lateinit var settings: NotificationSettings
    @MockK lateinit var analytics: AnalyticsSettings
    @MockK lateinit var generalNotifications: GeneralNotifications
    @MockK lateinit var presenceTracingNotifications: PresenceTracingNotifications
    @MockK lateinit var digitalCovidCertificateNotifications: DigitalCovidCertificateNotifications

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : NotificationSettingsFragmentViewModel.Factory {
                override fun create(): NotificationSettingsFragmentViewModel =
                    NotificationSettingsFragmentViewModel(
                        notificationSettings = settings,
                        dispatcherProvider = TestDispatcherProvider(),
                        generalNotifications = generalNotifications,
                        presenceTracingNotifications = presenceTracingNotifications,
                        digitalCovidCertificateNotifications = digitalCovidCertificateNotifications,
                    )
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Screenshot
    @Test
    fun notifications_enabled_screenshot() {
        every { settings.isNotificationsEnabled } returns flowOf(true)

        launchFragmentInContainer2<NotificationSettingsFragment>()
        takeScreenshot<NotificationSettingsFragment>("enabled")
    }

    @Screenshot
    @Test
    fun notifications_disabled_screenshot() {
        every { settings.isNotificationsEnabled } returns flowOf(false)

        launchFragmentInContainer2<NotificationSettingsFragment>()
        takeScreenshot<NotificationSettingsFragment>("disabled")
    }
}

@Module
abstract class NotificationSettingsFragmentModule {
    @ContributesAndroidInjector
    abstract fun notificationSettingsFragment(): NotificationSettingsFragment
}
