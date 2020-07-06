package de.rki.coronawarnapp.util

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.CoronaWarnApplication
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExternalActionHelperTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var applicationInfo: ApplicationInfo

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication)
    }

    @Test
    fun toConnectionsTest() {
        every { context.startActivity(any()) } just Runs
        ExternalActionHelper.toConnections(context = context)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun toNotificationsTest() {
        every { context.startActivity(any()) } just Runs
        every { context.packageName } returns "package_name"
        every { context.applicationInfo } returns applicationInfo

        ExternalActionHelper.toNotifications(context = context)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun toMainSettingsTest() {
        every { context.startActivity(any()) } just Runs
        ExternalActionHelper.toConnections(context = context)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun openUrlTest() {
        val fragment = mockk<Fragment>()
        every { fragment.startActivity(any()) } just Runs
        ExternalActionHelper.openUrl(fragment = fragment, url = "url_path")
        verify(exactly = 1) { fragment.startActivity(any()) }
    }

    @Test
    fun callTest() {
        val fragment = mockk<Fragment>()
        every { fragment.startActivity(any()) } just Runs
        ExternalActionHelper.call(fragment = fragment, uri = "call_path")
        verify(exactly = 1) { fragment.startActivity(any()) }
    }

    @Test
    fun shareTextTest() {
        val fragment = mockk<Fragment>()
        every { fragment.startActivity(any()) } just Runs
        ExternalActionHelper.shareText(fragment = fragment, text = "text", title = "title")
        verify(exactly = 1) { fragment.startActivity(any()) }
    }

    @Test
    fun disableBatteryOptimizations() {
        every { context.packageName } returns "package_name"
        every { context.startActivity(any()) } just Runs
        ExternalActionHelper.disableBatteryOptimizations(context)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun toBatteryOptimizationSettings() {
        every { context.startActivity(any()) } just Runs
        ExternalActionHelper.toBatteryOptimizationSettings(context)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
