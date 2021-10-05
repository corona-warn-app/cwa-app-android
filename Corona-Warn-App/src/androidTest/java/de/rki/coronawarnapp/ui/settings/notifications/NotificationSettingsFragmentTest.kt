package de.rki.coronawarnapp.ui.settings.notifications

import androidx.lifecycle.asLiveData
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

class NotificationSettingsFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: NotificationSettingsFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : NotificationSettingsFragmentViewModel.Factory {
                override fun create(): NotificationSettingsFragmentViewModel = viewModel
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
        every { viewModel.notificationSettingsState } returns flowOf(NotificationSettingsState(true)).asLiveData()

        launchFragmentInContainer2<NotificationSettingsFragment>()
        takeScreenshot<NotificationSettingsFragment>("enabled")
    }

    @Screenshot
    @Test
    fun notifications_disabled_screenshot() {
        every { viewModel.notificationSettingsState } returns flowOf(NotificationSettingsState(true)).asLiveData()

        launchFragmentInContainer2<NotificationSettingsFragment>()
        takeScreenshot<NotificationSettingsFragment>("disabled")
    }
}

@Module
abstract class NotificationSettingsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun notificationSettingsFragment(): NotificationSettingsFragment
}
