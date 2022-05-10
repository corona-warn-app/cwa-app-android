package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SettingsFragmentViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var notificationSettings: NotificationSettings
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var analytics: Analytics

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { tracingStatus.generalStatus } returns flowOf(GeneralTracingStatus.Status.TRACING_ACTIVE)
        every { notificationSettings.isNotificationsEnabled } returns flow { emit(true) }
        every { backgroundModeStatus.isIgnoringBatteryOptimizations } returns flow { emit(true) }
        every { analytics.isAnalyticsEnabledFlow() } returns flow { emit(true) }
    }

    private fun createInstance(): SettingsFragmentViewModel = SettingsFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        tracingStatus = tracingStatus,
        backgroundModeStatus = backgroundModeStatus,
        notificationSettings = notificationSettings,
        analytics = analytics
    )

    @Test
    fun `tracing status is forwarded and mapped`() {
        createInstance().apply {
            tracingState.observeForever { }
            tracingState.value shouldBe SettingsTracingState.TracingActive
        }
        verify { tracingStatus.generalStatus }
    }

    @Test
    fun `notification status is forwarded`() {
        createInstance().apply {
            notificationSettingsState.observeForever { }
            notificationSettingsState.value shouldBe SettingsNotificationState(
                isNotificationsEnabled = true,
            )
        }
        verify { notificationSettings.isNotificationsEnabled }
    }

    @Test
    fun `background priority status is forwarded`() {
        createInstance().apply {
            backgroundPriorityState.observeForever { }
            backgroundPriorityState.value shouldBe SettingsBackgroundState(
                isEnabled = true
            )
        }
        verify { backgroundModeStatus.isIgnoringBatteryOptimizations }
    }

    @Test
    fun `analytics status is forwarded`() {
        createInstance().apply {
            analyticsState.observeForever { }
            analyticsState.value shouldBe SettingsPrivacyPreservingAnalyticsState(
                isEnabled = true
            )
        }
        verify { analytics.isAnalyticsEnabledFlow() }
    }
}
