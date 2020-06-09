package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
        bConnection: Boolean,
        iValue: Int
    ) {
        every { context.getString(R.string.settings_tracing_status_restricted) } returns R.string.settings_tracing_status_restricted.toString()
        every { context.getString(R.string.settings_tracing_status_active) } returns R.string.settings_tracing_status_active.toString()
        every { context.getString(R.string.settings_tracing_status_inactive) } returns R.string.settings_tracing_status_inactive.toString()

        val result = formatTracingStatusText(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
        )
        assertThat(result, `is`((context.getString(iValue))))
    }

    private fun formatTracingDescriptionBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean,
        iValue: Int
    ) {
        every { context.getString(R.string.settings_tracing_body_bluetooth_inactive) } returns R.string.settings_tracing_body_bluetooth_inactive.toString()
        every { context.getString(R.string.settings_tracing_body_connection_inactive) } returns R.string.settings_tracing_body_connection_inactive.toString()
        every { context.getString(R.string.settings_tracing_body_active) } returns R.string.settings_tracing_body_active.toString()
        every { context.getString(R.string.settings_tracing_body_inactive) } returns R.string.settings_tracing_body_inactive.toString()

        val result = formatTracingDescription(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
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
                    R.string.settings_notifications_headline_inactive
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
                    R.string.settings_notifications_body_inactive
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
        bConnection: Boolean,
        bValue: Boolean
    ) {
        val result = formatTracingSwitch(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
        )
        assertThat(
            result, `is`(bValue)
        )
    }

    private fun formatTracingSwitchEnabledBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean,
        bValue: Boolean
    ) {
        val result = formatTracingSwitchEnabled(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
        )
        assertThat(
            result, `is`(bValue)
        )
    }

    private fun formatTracingIconBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean
    ) {
        every { context.getDrawable(R.drawable.ic_settings_tracing_bluetooth_inactive) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_tracing_connection_inactive) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_tracing_active) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_tracing_inactive) } returns drawable

        val result =
            formatTracingIcon(tracing = bTracing, bluetooth = bBluetooth, connection = bConnection)
        assertThat(
            result, `is`(CoreMatchers.equalTo(drawable))
        )
    }

    private fun formatTracingIconColorBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean,
        iColor: Int
    ) {
        every { context.getColor(R.color.colorAccentTintIcon) } returns R.color.colorAccentTintIcon
        every { context.getColor(R.color.colorTextSemanticRed) } returns R.color.colorTextSemanticRed

        val result = formatTracingIconColor(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
        )
        assertThat(
            result, `is`(context.getColor(iColor))
        )
    }

    private fun formatTracingStatusImageBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean
    ) {
        every { context.getDrawable(R.drawable.ic_settings_illustration_bluetooth_off) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_illustration_connection_off) } returns drawable
        every { context.getDrawable(R.drawable.ic_illustration_tracing_on) } returns drawable
        every { context.getDrawable(R.drawable.ic_settings_illustration_tracing_off) } returns drawable

        val result = formatTracingStatusImage(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
        )
        assertThat(
            result, `is`(CoreMatchers.equalTo(drawable))
        )
    }

    private fun formatTracingStatusConnectionBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean
    ) {
        val result = formatTracingStatusConnection(
            tracing = bTracing,
            bluetooth = bBluetooth,
            connection = bConnection
        )
        assertThat(true, `is`(result > -1))
    }

    private fun formatTracingStatusVisibilityTracingBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean
    ) {
        val result =
            formatTracingStatusVisibilityTracing(
                tracing = bTracing,
                bluetooth = bBluetooth,
                connection = bConnection
            )
        assertThat(true, `is`(result > -1))
    }

    private fun formatTracingStatusVisibilityBluetoothBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean
    ) {
        val result =
            formatTracingStatusVisibilityBluetooth(
                tracing = bTracing,
                bluetooth = bBluetooth,
                connection = bConnection
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
        // When tracing is true, bluetooth is true, connection is true
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = true,
            iValue = R.string.settings_tracing_status_active
        )

        // When tracing is false, bluetooth is false, connection is false
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false,
            iValue = R.string.settings_tracing_status_inactive
        )

        // When tracing is true, bluetooth is false, connection is false
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false,
            iValue = R.string.settings_tracing_status_restricted
        )

        // When tracing is true, bluetooth is true, connection is false
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = false,
            iValue = R.string.settings_tracing_status_restricted
        )

        // When tracing is false, bluetooth is true, connection is false
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false,
            iValue = R.string.settings_tracing_status_inactive
        )

        // When tracing is false, bluetooth is true, connection is true
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true,
            iValue = R.string.settings_tracing_status_inactive
        )

        // When tracing is true, bluetooth is false, connection is true
        formatTracingStatusBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = true,
            iValue = R.string.settings_tracing_status_restricted
        )

        // When tracing is false, bluetooth is false, connection is true
        formatTracingStatusBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true,
            iValue = R.string.settings_tracing_status_inactive
        )
    }

    @Test
    fun formatTracingDescription() {
        // When tracing is true, bluetooth is true, connection is true
        formatTracingDescriptionBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = true,
            iValue = R.string.settings_tracing_body_active
        )

        // When tracing is false, bluetooth is false, connection is false
        formatTracingDescriptionBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false,
            iValue = R.string.settings_tracing_body_inactive
        )

        // When tracing is true, bluetooth is false, connection is false
        formatTracingDescriptionBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false,
            iValue = R.string.settings_tracing_body_bluetooth_inactive
        )

        // When tracing is true, bluetooth is true, connection is false
        formatTracingDescriptionBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = false,
            iValue = R.string.settings_tracing_body_connection_inactive
        )

        // When tracing is false, bluetooth is true, connection is false
        formatTracingDescriptionBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false,
            iValue = R.string.settings_tracing_body_inactive
        )

        // When tracing is false, bluetooth is true, connection is true
        formatTracingDescriptionBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true,
            iValue = R.string.settings_tracing_body_inactive
        )

        // When tracing is true, bluetooth is false, connection is true
        formatTracingDescriptionBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = true,
            iValue = R.string.settings_tracing_body_bluetooth_inactive
        )

        // When tracing is false, bluetooth is false, connection is true
        formatTracingDescriptionBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true,
            iValue = R.string.settings_tracing_body_inactive
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
            bConnection = true,
            bValue = true
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = true,
            bValue = false
        )

        formatTracingSwitchBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = false,
            bValue = false
        )
    }

    @Test
    fun formatTracingSwitchEnabled() {

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true,
            bValue = true
        )

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false,
            bValue = false
        )

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = true,
            bValue = false
        )

        formatTracingSwitchEnabledBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = false,
            bValue = false
        )
    }

    @Test
    fun formatTracingIcon() {
        formatTracingIconBase(bTracing = true, bBluetooth = true, bConnection = true)

        formatTracingIconBase(bTracing = false, bBluetooth = false, bConnection = false)

        formatTracingIconBase(bTracing = false, bBluetooth = false, bConnection = true)

        formatTracingIconBase(bTracing = false, bBluetooth = true, bConnection = false)

        formatTracingIconBase(bTracing = false, bBluetooth = true, bConnection = true)

        formatTracingIconBase(bTracing = true, bBluetooth = false, bConnection = false)

        formatTracingIconBase(bTracing = true, bBluetooth = false, bConnection = true)

        formatTracingIconBase(bTracing = true, bBluetooth = true, bConnection = false)
    }

    @Test
    fun formatTracingIconColor() {

        formatTracingIconColorBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = true,
            iColor = R.color.colorAccentTintIcon
        )

        formatTracingIconColorBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false,
            iColor = R.color.colorTextSemanticRed
        )

        formatTracingIconColorBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true,
            iColor = R.color.colorTextSemanticRed
        )

        formatTracingIconColorBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false,
            iColor = R.color.colorTextSemanticRed
        )

        formatTracingIconColorBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true,
            iColor = R.color.colorTextSemanticRed
        )

        formatTracingIconColorBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false,
            iColor = R.color.colorTextSemanticRed
        )

        formatTracingIconColorBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = true,
            iColor = R.color.colorTextSemanticRed
        )

        formatTracingIconColorBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = false,
            iColor = R.color.colorTextSemanticRed
        )
    }

    @Test
    fun formatTracingStatusImage() {
        formatTracingStatusImageBase(bTracing = true, bBluetooth = true, bConnection = true)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = false, bConnection = false)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = false, bConnection = true)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = true, bConnection = false)

        formatTracingStatusImageBase(bTracing = false, bBluetooth = true, bConnection = true)

        formatTracingStatusImageBase(bTracing = true, bBluetooth = false, bConnection = false)

        formatTracingStatusImageBase(bTracing = true, bBluetooth = false, bConnection = true)

        formatTracingStatusImageBase(bTracing = true, bBluetooth = true, bConnection = false)
    }

    @Test
    fun formatTracingStatusConnection() {
        formatTracingStatusConnectionBase(bTracing = true, bBluetooth = true, bConnection = true)

        formatTracingStatusConnectionBase(bTracing = false, bBluetooth = false, bConnection = false)

        formatTracingStatusConnectionBase(bTracing = false, bBluetooth = false, bConnection = true)

        formatTracingStatusConnectionBase(bTracing = false, bBluetooth = true, bConnection = false)

        formatTracingStatusConnectionBase(bTracing = false, bBluetooth = true, bConnection = true)

        formatTracingStatusConnectionBase(bTracing = true, bBluetooth = false, bConnection = false)

        formatTracingStatusConnectionBase(bTracing = true, bBluetooth = false, bConnection = true)

        formatTracingStatusConnectionBase(bTracing = true, bBluetooth = true, bConnection = false)
    }

    @Test
    fun formatTracingStatusVisibilityBluetooth() {
        formatTracingStatusVisibilityBluetoothBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true
        )

        formatTracingStatusVisibilityBluetoothBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false
        )

        formatTracingStatusConnectionBase(bTracing = true, bBluetooth = false, bConnection = true)

        formatTracingStatusConnectionBase(bTracing = true, bBluetooth = true, bConnection = false)
    }

    @Test
    fun formatTracingStatusVisibilityTracing() {
        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = false
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = false,
            bConnection = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = false
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = false,
            bBluetooth = true,
            bConnection = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = false
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = false,
            bConnection = true
        )

        formatTracingStatusVisibilityTracingBase(
            bTracing = true,
            bBluetooth = true,
            bConnection = false
        )
    }

    @Test
    fun formatNotificationImage() {
        formatNotificationImageBase(bNotifications = true)

        formatNotificationImageBase(bNotifications = false)
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
