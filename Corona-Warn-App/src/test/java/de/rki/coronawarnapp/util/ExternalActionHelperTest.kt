package de.rki.coronawarnapp.util

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.ExternalActionHelper.callPhone
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ExternalActionHelper.shareText
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest

class ExternalActionHelperTest : BaseTest() {

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
    fun toNotificationsTest() {
        every { context.startActivity(any()) } just Runs
        every { context.packageName } returns "package_name"
        every { context.applicationInfo } returns applicationInfo

        ExternalActionHelper.toNotifications(context = context)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun openUrlTest() {
        mockk<Fragment>().apply {
            every { startActivity(any()) } just Runs
            openUrl(url = "url_path")
            verify(exactly = 1) { startActivity(any()) }
        }
    }

    @Test
    fun callTest() {
        mockk<Fragment>().apply {
            every { startActivity(any()) } just Runs
            callPhone(phoneNumber = "01234343535")
            verify(exactly = 1) { startActivity(any()) }
        }
    }

    @Test
    fun shareTextTest() {
        mockk<Fragment>().apply {
            every { startActivity(any()) } just Runs
            shareText(text = "text", title = "title")
            verify(exactly = 1) { startActivity(any()) }
        }
    }
}
