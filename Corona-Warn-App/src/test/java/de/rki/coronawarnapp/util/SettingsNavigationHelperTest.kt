package de.rki.coronawarnapp.util

import android.content.Context
import android.content.pm.ApplicationInfo
import de.rki.coronawarnapp.CoronaWarnApplication
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class SettingsNavigationHelperTest {

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

    @After
    fun cleanUp() {
        unmockkAll()
    }
}