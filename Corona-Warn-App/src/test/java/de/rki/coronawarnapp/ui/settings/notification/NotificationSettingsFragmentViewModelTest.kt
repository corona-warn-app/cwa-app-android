package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentViewModel
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class NotificationSettingsFragmentViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var notificationSettings: NotificationSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
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
    fun `toggle risk notifications`() {
        TODO()
    }

    @Test
    fun `toggle test notifications`() {
        TODO()
    }
}
