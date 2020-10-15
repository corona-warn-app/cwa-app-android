package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class FormatterSettingsHelperTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var drawable: Drawable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { context.getString(R.string.settings_on) } returns "settings_on"
        every { context.getString(R.string.settings_off) } returns "settings_off"
        every { context.getString(R.string.settings_notifications_headline_active) } returns "settings_notifications_headline_active"
        every { context.getString(R.string.settings_notifications_headline_inactive) } returns "settings_notifications_headline_inactive"
        every { context.getString(R.string.settings_notifications_body_active) } returns "settings_notifications_body_active"
        every { context.getString(R.string.settings_notifications_body_inactive) } returns "settings_notifications_body_inactive"
        every { context.getColor(R.color.colorAccentTintIcon) } returns R.color.colorAccentTintIcon
        every { context.getColor(R.color.colorTextPrimary3) } returns R.color.colorTextPrimary3
    }

    private fun formatStatusBase(bValue: Boolean) {
        val result = formatStatus(value = bValue)
        assertThat(
            result, `is`(
                (formatText(
                    bValue,
                    R.string.settings_on,
                    R.string.settings_off
                ))
            )
        )
    }

    private fun formatNotificationsStatusTextBase(
        bNotifications: Boolean,
        bNotificationsRisk: Boolean,
        bNotificationsTest: Boolean,
        bValue: Boolean
    ) {
        val result = formatNotificationsStatusText(
            notifications = bNotifications,
            notificationsRisk = bNotificationsRisk,
            notificationsTest = bNotificationsTest
        )
        assertThat(result, `is`((formatStatus(bValue))))
    }

    private fun formatTracingStatusBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bLocation: Boolean,
        iValue: Int
    ) {
        every { context.getString(R.string.settings_tracing_status_restricted) } returns R.string.settings_tracing_status_restricted.toString()
        every { context.getString(R.string.settings_tracing_status_active) } returns R.string.settings_tracing_status_active.toString()
        every { context.getString(R.string.settings_tracing_status_inactive) } returns R.string.settings_tracing_status_inactive.toString()

        val result = formatTracingStatusText(
            tracing = bTracing,
            bluetooth = bBluetooth,
            location = bLocation
        )
        assertThat(result, `is`((context.getString(iValue))))
    }

    private fun formatNotificationsTitleBase(bValue: Boolean) {
        val result = formatNotificationsTitle(notifications = bValue)
        assertThat(
            result, `is`(
                (formatText(
                    bValue,
                    R.string.settings_notifications_headline_active,
                    null
                ))
            )
        )
    }

    private fun formatNotificationsDescriptionBase(bValue: Boolean) {
        val result = formatNotificationsDescription(notifications = bValue)
        assertThat(
            result, `is`(
                (formatText(
                    bValue,
                    R.string.settings_notifications_body_active,
                    null
                ))
            )
        )
    }

    private fun formatIconColorBase(bActive: Boolean) {
        val result = formatIconColor(active = bActive)
        assertThat(
            result, `is`(
                (formatColor(bActive, R.color.colorAccentTintIcon, R.color.colorTextPrimary3))
            )
        )
    }

    private fun formatTracingSwitchBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bLocation: Boolean,
        bValue: Boolean
    ) {
        val result = formatTracingSwitch(
            tracing = bTracing,
            bluetooth = bBluetooth,
            location = bLocation
        )
        assertThat(
            result, `is`(bValue)
        )
    }

    private fun formatTracingSwitchEnabledBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bLocation: Boolean,
        bValue: Boolean
    ) {
        val result = formatTracingSwitchEnabled(
            tracing = bTracing,
            bluetooth = bBluetooth,
            location = bLocation
        )
        assertThat(
            result, `is`(bValue)
        )
    }

    private fun formatTracingStatusImageBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bLocation: Boolean
    ) {
        every { context.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off) } returns drawable
        every { context.getDrawable(R.drawable.ic_illustration_tracing_on) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_illustration_tracing_off) } returns drawable

        val result = formatTracingStatusImage(
            tracing = bTracing,
            bluetooth = bBluetooth,
            location = bLocation
        )
        assertThat(
            result, `is`(CoreMatchers.equalTo(drawable))
        )
    }

    private fun formatTracingStatusVisibilityTracingBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bLocation: Boolean
    ) {
        val result =
            formatTracingStatusVisibilityTracing(
                tracing = bTracing,
                bluetooth = bBluetooth,
                location = bLocation
            )
        assertThat(true, `is`(result > -1))
    }

    private fun formatTracingStatusVisibilityBluetoothBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bLocation: Boolean
    ) {
        val result =
            formatTracingStatusVisibilityBluetooth(
                tracing = bTracing,
                bluetooth = bBluetooth,
                location = bLocation
            )
        assertThat(true, `is`(result > -1))
    }

    private fun formatNotificationImageBase(bNotifications: Boolean) {
        every { context.getDrawable(R.drawable.ic_illustration_notification_on) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_illustration_notification_off) } returns drawable

        val result = formatDrawable(
            bNotifications,
            R.drawable.ic_illustration_notification_on,
            R.drawable.ic_settings_illustration_notification_off
        )
        assertThat(
            result, `is`(CoreMatchers.equalTo(drawable))
        )
    }

    @Test
    fun formatStatus() {
        // When status true
        formatStatusBase(true)

        // When status false
        formatStatusBase(false)
    }

    @Test
    fun formatNotificationsStatusText() {
        // When notifications is true, notificationsRisk is true, notificationsTest is true
        formatNotificationsStatusTextBase(
            bNotifications = true,
            bNotificationsRisk = true,
            bNotificationsTest = true, bValue = true
        )

        // When notifications is false, notificationsRisk is false, notificationsTest is false
        formatNotificationsStatusTextBase(
            bNotifications = false,
            bNotificationsRisk = true,
            bNotificationsTest = true,
            bValue = false
        )

        // When notifications is true, notificationsRisk is false, notificationsTest is true
        formatNotificationsStatusTextBase(
            bNotifications = true,
            bNotificationsRisk = false,
            bNotificationsTest = true,
            bValue = true
        )

        // When notifications is true, notificationsRisk is true, notificationsTest is false
        formatNotificationsStatusTextBase(
            bNotifications = true,
            bNotificationsRisk = true,
            bNotificationsTest = false,
            bValue = true
        )

        // When notifications is true, notificationsRisk is false, notificationsTest is false
        formatNotificationsStatusTextBase(
            bNotifications = true,
            bNotificationsRisk = false,
            bNotificationsTest = false,
            bValue = false
        )

        // When notifications is false, notificationsRisk is false, notificationsTest is false
        formatNotificationsStatusTextBase(
            bNotifications = false,
            bNotificationsRisk = false,
            bNotificationsTest = false,
            bValue = false
        )

        // When notifications is false, notificationsRisk is true, notificationsTest is false
        formatNotificationsStatusTextBase(
            bNotifications = false,
            bNotificationsRisk = true,
            bNotificationsTest = false,
            bValue = false
        )

        // When notifications is false, notificationsRisk is false, notificationsTest is true
        formatNotificationsStatusTextBase(
            bNotifications = false,
            bNotificationsRisk = false,
            bNotificationsTest = true,
            bValue = false
        )
    }

    @Test
    fun formatTracingStatusText() {
        // When tracing is true, bluetooth is true, location is true
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true,
            iValue = R.string.settings_tracing_status_active
        )

        // When tracing is false, bluetooth is false, location is false
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = false,
            iValue = R.string.settings_tracing_status_inactive
        )

        // When tracing is true, bluetooth is false, location is true
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true,
            iValue = R.string.settings_tracing_status_restricted
        )

        // When tracing is true, bluetooth is true, location is true
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true,
            iValue = R.string.settings_tracing_status_active
        )

        // When tracing is false, bluetooth is true, location is false
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = false,
            iValue = R.string.settings_tracing_status_inactive
        )

        // When tracing is false, bluetooth is true, location is true
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true,
            iValue = R.string.settings_tracing_status_inactive
        )

        // When tracing is true, bluetooth is false, location is true
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true,
            iValue = R.string.settings_tracing_status_restricted
        )

        // When tracing is false, bluetooth is false, location is true
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true,
            iValue = R.string.settings_tracing_status_inactive
        )
    }

    @Test
    fun formatNotificationsTitle() {
        // When status true
        formatNotificationsTitleBase(bValue = true)

        // When status false
        formatNotificationsTitleBase(bValue = false)
    }

    @Test
    fun formatNotificationsDescription() {
        // When status true
        formatNotificationsDescriptionBase(bValue = true)

        // When status false
        formatNotificationsDescriptionBase(bValue = false)
    }

    @Test
    fun formatIconColor() {
        // When status true
        formatIconColorBase(bActive = true)

        // When status false
        formatIconColorBase(bActive = false)
    }

    @Test
    fun formatTracingSwitch() {
        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = false,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = false,
            bValue = false
        )
    }

    @Test
    fun formatTracingSwitchEnabled() {

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true,
            bValue = false
        )

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true,
            bValue = true
        )
    }

    @Test
    fun formatTracingStatusImage() {
        formatTracingStatusImageBase(bTracing = true, bBluetooth = true, bLocation = true)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = false, bLocation = true)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = false, bLocation = true)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = true, bLocation = true)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = true, bLocation = true)

        formatTracingStatusImageBase(bTracing = true, bBluetooth = false, bLocation = true)

        formatTracingStatusImageBase(bTracing = true, bBluetooth = false, bLocation = true)

        formatTracingStatusImageBase(bTracing = true, bBluetooth = true, bLocation = true)
    }

    @Test
    fun formatTracingStatusVisibilityBluetooth() {
        formatTracingStatusVisibilityBluetoothBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true
        )
    }

    @Test
    fun formatTracingStatusVisibilityTracing() {
        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = false,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = true,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = false,
            bLocation = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = true,
            bLocation = true
        )
    }

    @Test
    fun formatNotificationImage() {
        formatNotificationImageBase(bNotifications = true)

        formatNotificationImageBase(bNotifications = false)
    }

    @Test
    fun formatSettingsBackgroundPriorityIconColor() {
        formatSettingsBackgroundPriorityIconColorBase(true, R.color.colorAccentTintIcon)
        formatSettingsBackgroundPriorityIconColorBase(false, R.color.colorTextSemanticRed)
    }

    private fun formatSettingsBackgroundPriorityIconColorBase(
        enabled: Boolean,
        expectedColor: Int
    ) {
        every { context.getColor(R.color.colorAccentTintIcon) } returns R.color.colorAccentTintIcon
        every { context.getColor(R.color.colorTextSemanticRed) } returns R.color.colorTextSemanticRed

        val result =
            formatSettingsBackgroundPriorityIconColor(enabled)
        assertThat(
            result, `is`(context.getColor(expectedColor))
        )
    }

    @Test
    fun formatSettingsBackgroundPriorityIcon() {
        formatSettingsBackgroundPriorityIconBase(
            true,
            R.drawable.ic_settings_background_priority_enabled
        )
        formatSettingsBackgroundPriorityIconBase(
            false,
            R.drawable.ic_settings_background_priority_disabled
        )
    }

    private fun formatSettingsBackgroundPriorityIconBase(
        enabled: Boolean,
        expectedDrawable: Int
    ) {
        val drawableA = mockk<Drawable>()
        val drawableB = mockk<Drawable>()

        every { context.getDrawable(R.drawable.ic_settings_background_priority_enabled) } returns drawableA
        every { context.getDrawable(R.drawable.ic_settings_background_priority_disabled) } returns drawableB

        val result =
            formatSettingsBackgroundPriorityIcon(enabled)
        assertThat(
            result, `is`(context.getDrawable(expectedDrawable))
        )
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
