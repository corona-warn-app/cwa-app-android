package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
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
class SettingsFragmentViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var tracing: GeneralTracingStatus
    @MockK lateinit var settingsRepository: SettingsRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): SettingsFragmentViewModel = SettingsFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider,
        tracingStatus = tracing,
        settingsRepository = settingsRepository
    )

    @Test
    fun `tracing status is forwarded`() {
        TODO()
    }

    @Test
    fun `notification status is forwarded`() {
        TODO()
        //  private fun formatNotificationsStatusTextBase(
        //        bNotifications: Boolean,
        //        bNotificationsRisk: Boolean,
        //        bNotificationsTest: Boolean,
        //        bValue: Boolean
        //    ) {
        //        val result = formatNotificationsStatusText(
        //            notifications = bNotifications,
        //            notificationsRisk = bNotificationsRisk,
        //            notificationsTest = bNotificationsTest
        //        )
        //        assertThat(result, `is`((formatStatus(bValue))))
        //    }
        //
        //    @Test
        //    fun formatNotificationsStatusText() {
        //        // When notifications is true, notificationsRisk is true, notificationsTest is true
        //        formatNotificationsStatusTextBase(
        //            bNotifications = true,
        //            bNotificationsRisk = true,
        //            bNotificationsTest = true, bValue = true
        //        )
        //
        //        // When notifications is false, notificationsRisk is false, notificationsTest is false
        //        formatNotificationsStatusTextBase(
        //            bNotifications = false,
        //            bNotificationsRisk = true,
        //            bNotificationsTest = true,
        //            bValue = false
        //        )
        //
        //        // When notifications is true, notificationsRisk is false, notificationsTest is true
        //        formatNotificationsStatusTextBase(
        //            bNotifications = true,
        //            bNotificationsRisk = false,
        //            bNotificationsTest = true,
        //            bValue = true
        //        )
        //
        //        // When notifications is true, notificationsRisk is true, notificationsTest is false
        //        formatNotificationsStatusTextBase(
        //            bNotifications = true,
        //            bNotificationsRisk = true,
        //            bNotificationsTest = false,
        //            bValue = true
        //        )
        //
        //        // When notifications is true, notificationsRisk is false, notificationsTest is false
        //        formatNotificationsStatusTextBase(
        //            bNotifications = true,
        //            bNotificationsRisk = false,
        //            bNotificationsTest = false,
        //            bValue = false
        //        )
        //
        //        // When notifications is false, notificationsRisk is false, notificationsTest is false
        //        formatNotificationsStatusTextBase(
        //            bNotifications = false,
        //            bNotificationsRisk = false,
        //            bNotificationsTest = false,
        //            bValue = false
        //        )
        //
        //        // When notifications is false, notificationsRisk is true, notificationsTest is false
        //        formatNotificationsStatusTextBase(
        //            bNotifications = false,
        //            bNotificationsRisk = true,
        //            bNotificationsTest = false,
        //            bValue = false
        //        )
        //
        //        // When notifications is false, notificationsRisk is false, notificationsTest is true
        //        formatNotificationsStatusTextBase(
        //            bNotifications = false,
        //            bNotificationsRisk = false,
        //            bNotificationsTest = true,
        //            bValue = false
        //        )
        //    }
    }

    @Test
    fun `background priority status is forwarded`() {
        TODO()
//         private fun formatSettingsBackgroundPriorityIconColorBase(
//        enabled: Boolean,
//        expectedColor: Int
//    ) {
//        every { context.getColor(R.color.colorAccentTintIcon) } returns R.color.colorAccentTintIcon
//        every { context.getColor(R.color.colorTextSemanticRed) } returns R.color.colorTextSemanticRed
//
//        val result =
//            formatSettingsBackgroundPriorityIconColor(enabled)
//        assertThat(
//            result, `is`(context.getColor(expectedColor))
//        )
//    }
//    @Test
//    fun formatSettingsBackgroundPriorityIconColor() {
//        formatSettingsBackgroundPriorityIconColorBase(true, R.color.colorAccentTintIcon)
//        formatSettingsBackgroundPriorityIconColorBase(false, R.color.colorTextSemanticRed)
//    }
//
//
//
//    @Test
//    fun formatSettingsBackgroundPriorityIcon() {
//        formatSettingsBackgroundPriorityIconBase(
//            true,
//            R.drawable.ic_settings_background_priority_enabled
//        )
//        formatSettingsBackgroundPriorityIconBase(
//            false,
//            R.drawable.ic_settings_background_priority_disabled
//        )
//    }
//
//    private fun formatSettingsBackgroundPriorityIconBase(
//        enabled: Boolean,
//        expectedDrawable: Int
//    ) {
//        val drawableA = mockk<Drawable>()
//        val drawableB = mockk<Drawable>()
//
//        every { context.getDrawable(R.drawable.ic_settings_background_priority_enabled) } returns drawableA
//        every { context.getDrawable(R.drawable.ic_settings_background_priority_disabled) } returns drawableB
//
//        val result =
//            formatSettingsBackgroundPriorityIcon(enabled)
//        assertThat(
//            result, `is`(context.getDrawable(expectedDrawable))
//        )
//    }
    }
}
