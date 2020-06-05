package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class FormatterSettingsHelperTest {

    @MockK
    private lateinit var coronaWarnApplication: CoronaWarnApplication

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { context.getString(R.string.settings_on) } returns "true string"
        every { context.getString(R.string.settings_off) } returns "false string"


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

    private fun formatTracingStatusBase(bTracing: Boolean, bBluetooth: Boolean, bConnection: Boolean, iValue: Int) {
        every { context.getString(R.string.settings_tracing_status_restricted) } returns R.string.settings_tracing_status_restricted.toString()
        every { context.getString(R.string.settings_tracing_status_active) } returns R.string.settings_tracing_status_active.toString()
        every { context.getString(R.string.settings_tracing_status_inactive) } returns R.string.settings_tracing_status_inactive.toString()

        val result = formatTracingStatusText(tracing = bTracing, bluetooth = bBluetooth, connection = bConnection)
        assertThat(result, `is`((context.getString(iValue))))
    }

    private fun formatTracingDescriptionBase(
        bTracing: Boolean,
        bBluetooth: Boolean,
        bConnection: Boolean,
        iValue: Int
    ) {
        every { context.getString(R.string.settings_tracing_status_restricted) } returns R.string.settings_tracing_status_restricted.toString()
        every { context.getString(R.string.settings_tracing_status_active) } returns R.string.settings_tracing_status_active.toString()
        every { context.getString(R.string.settings_tracing_status_inactive) } returns R.string.settings_tracing_status_inactive.toString()

        val result = formatTracingDescription(tracing = bTracing, bluetooth = bBluetooth, connection = bConnection)
        assertThat(result, `is`((context.getString(iValue))))
    }

    @Test
    fun formatStatus() {
        // when status true
        formatStatusBase(true)

        // when status false
        formatStatusBase(false)
    }

    @Test
    fun formatNotificationsStatusText() {
        // when notifications is true, notificationsRisk is true, notificationsTest is true
        formatNotificationsStatusTextBase(true, true, true, true)

        // when notifications is false, notificationsRisk is false, notificationsTest is false
        formatNotificationsStatusTextBase(false, true, true, false)

        // when notifications is true, notificationsRisk is false, notificationsTest is true
        formatNotificationsStatusTextBase(true, false, true, true)

        // when notifications is true, notificationsRisk is true, notificationsTest is false
        formatNotificationsStatusTextBase(true, true, false, true)

        // when notifications is true, notificationsRisk is false, notificationsTest is false
        formatNotificationsStatusTextBase(true, false, false, false)

        // when notifications is false, notificationsRisk is false, notificationsTest is false
        formatNotificationsStatusTextBase(false, false, false, false)

        // when notifications is false, notificationsRisk is true, notificationsTest is false
        formatNotificationsStatusTextBase(false, true, false, false)

        // when notifications is false, notificationsRisk is false, notificationsTest is true
        formatNotificationsStatusTextBase(false, false, true, false)

    }

    @Test
    fun formatTracingDescription() {
        // when tracing is true, bluetooth is true, connection is true
        formatTracingDescriptionBase(true, true, true, R.string.settings_tracing_status_active)

        // when tracing is false, bluetooth is false, connection is false
        formatTracingDescriptionBase(false, false, false, R.string.settings_tracing_status_inactive)

        // when tracing is true, bluetooth is false, connection is false
        formatTracingDescriptionBase(true, false, false, R.string.settings_tracing_status_restricted)

        // when tracing is true, bluetooth is true, connection is false
        formatTracingDescriptionBase(true, true, false, R.string.settings_tracing_status_restricted)

        // when tracing is false, bluetooth is true, connection is false
        formatTracingDescriptionBase(false, true, false, R.string.settings_tracing_status_inactive)

        // when tracing is false, bluetooth is true, connection is true
        formatTracingDescriptionBase(false, true, true, R.string.settings_tracing_status_inactive)

        // when tracing is true, bluetooth is false, connection is true
        formatTracingDescriptionBase(true, false, true, R.string.settings_tracing_status_active)

        // when tracing is false, bluetooth is false, connection is true
        formatTracingDescriptionBase(false, false, true, R.string.settings_tracing_status_inactive)

    }

    @Test
    fun formatTracingStatusText() {
        // when tracing is true, bluetooth is true, connection is true
        formatTracingStatusBase(true, true, true, R.string.settings_tracing_status_active)

        // when tracing is false, bluetooth is false, connection is false
        formatTracingStatusBase(false, false, false, R.string.settings_tracing_status_inactive)

        // when tracing is true, bluetooth is false, connection is false
        formatTracingStatusBase(true, false, false, R.string.settings_tracing_status_restricted)

        // when tracing is true, bluetooth is true, connection is false
        formatTracingStatusBase(true, true, false, R.string.settings_tracing_status_restricted)

        // when tracing is false, bluetooth is true, connection is false
        formatTracingStatusBase(false, true, false, R.string.settings_tracing_status_inactive)

        // when tracing is false, bluetooth is true, connection is true
        formatTracingStatusBase(false, true, true, R.string.settings_tracing_status_inactive)

        // when tracing is true, bluetooth is false, connection is true
        formatTracingStatusBase(true, false, true, R.string.settings_tracing_status_active)

        // when tracing is false, bluetooth is false, connection is true
        formatTracingStatusBase(false, false, true, R.string.settings_tracing_status_inactive)

    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
