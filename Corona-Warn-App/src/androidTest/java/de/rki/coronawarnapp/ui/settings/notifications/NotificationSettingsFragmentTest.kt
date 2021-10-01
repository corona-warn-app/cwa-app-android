package de.rki.coronawarnapp.ui.settings.notifications

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
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

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : NotificationSettingsFragmentViewModel.Factory {
                override fun create(): NotificationSettingsFragmentViewModel =
                    NotificationSettingsFragmentViewModel(
                        notificationSettings = settings,
                        dispatcherProvider = TestDispatcherProvider(),
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
