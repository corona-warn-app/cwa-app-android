package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SettingsNotificationStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `state mapping`() {
        TODO()
    }

    @Test
    fun `bluetooth disabled`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `location disabled`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `tracing inactive`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `tracing active`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

// private fun formatNotificationsTitleBase(bValue: Boolean) {
//        val result = formatNotificationsTitle(notifications = bValue)
//        assertThat(
//            result, `is`(
//                (formatText(
//                    bValue,
//                    R.string.settings_notifications_headline_active,
//                    null
//                ))
//            )
//        )
//    }
//
//    private fun formatNotificationsDescriptionBase(bValue: Boolean) {
//        val result = formatNotificationsDescription(notifications = bValue)
//        assertThat(
//            result, `is`(
//                (formatText(
//                    bValue,
//                    R.string.settings_notifications_body_active,
//                    null
//                ))
//            )
//        )
//    }
//        @Test
//    fun formatNotificationsTitle() {
//        // When status true
//        formatNotificationsTitleBase(bValue = true)
//
//        // When status false
//        formatNotificationsTitleBase(bValue = false)
//    }
//
//    @Test
//    fun formatNotificationsDescription() {
//        // When status true
//        formatNotificationsDescriptionBase(bValue = true)
//
//        // When status false
//        formatNotificationsDescriptionBase(bValue = false)
//    }
//        private fun formatStatusBase(bValue: Boolean) {
//        val result = formatStatus(value = bValue)
//        assertThat(
//            result, `is`(
//                (formatText(
//                    bValue,
//                    R.string.settings_on,
//                    R.string.settings_off
//                ))
//            )
//        )
//    }
//
//    private fun formatIconColorBase(bActive: Boolean) {
//        val result = formatIconColor(active = bActive)
//        assertThat(
//            result, `is`(
//                (formatColor(bActive, R.color.colorAccentTintIcon, R.color.colorTextPrimary3))
//            )
//        )
//    }
//
//    private fun formatNotificationImageBase(bNotifications: Boolean) {
//        every { context.getDrawable(R.drawable.ic_illustration_notification_on) } returns drawable
//        every { context.getDrawable(R.drawable.ic_settings_illustration_notification_off) } returns drawable
//
//        val result = formatDrawable(
//            bNotifications,
//            R.drawable.ic_illustration_notification_on,
//            R.drawable.ic_settings_illustration_notification_off
//        )
//        assertThat(
//            result, `is`(CoreMatchers.equalTo(drawable))
//        )
//    }
//
//    @Test
//    fun formatStatus() {
//        // When status true
//        formatStatusBase(true)
//
//        // When status false
//        formatStatusBase(false)
//    }
//
//    @Test
//    fun formatIconColor() {
//        // When status true
//        formatIconColorBase(bActive = true)
//
//        // When status false
//        formatIconColorBase(bActive = false)
//    }
//
//    @Test
//    fun formatNotificationImage() {
//        formatNotificationImageBase(bNotifications = true)
//
//        formatNotificationImageBase(bNotifications = false)
//    }
}
