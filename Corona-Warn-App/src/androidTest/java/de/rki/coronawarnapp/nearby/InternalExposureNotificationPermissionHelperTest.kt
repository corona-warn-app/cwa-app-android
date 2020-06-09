package de.rki.coronawarnapp.nearby

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.TestFragment
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * InternalExposureNotificationPermissionHelper test.
 */
@RunWith(AndroidJUnit4::class)
class InternalExposureNotificationPermissionHelperTest {
    private lateinit var scenario: FragmentScenario<TestFragment>
    private var fail = false
    private var startSuccess = false
    private var sharingSuccess = false
    private val callback = object : InternalExposureNotificationPermissionHelper.Callback {
        override fun onFailure(exception: Exception?) {
            fail = true
        }

        override fun onStartPermissionGranted() {
            startSuccess = true
        }

        override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
            sharingSuccess = true
        }
    }

    /**
     * Launch test fragment, required for view lifecycle owner.
     *
     * @see [InternalExposureNotificationPermissionHelper]
     * @see [Fragment.getViewLifecycleOwner]
     */
    @Before
    fun setUp() {
        fail = false
        startSuccess = false
        sharingSuccess = false
        mockkObject(InternalExposureNotificationClient)
        scenario = launchFragmentInContainer<TestFragment>()
    }

    /**
     * Test tracing permission request assuming EN Client is enabled.
     */
    @Test
    fun testRequestPermissionToStartTracingENIsEnabled() {
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true
        scenario.onFragment {
            val helper = InternalExposureNotificationPermissionHelper(it, callback)
            helper.requestPermissionToStartTracing()
        }
        assertThat(fail, `is`(false))
        assertThat(startSuccess, `is`(true))
    }

    /**
     * Test tracing permission request assuming EN Client is disabled.
     */
    @Test
    fun testRequestPermissionToStartTracingENIsNotEnabled() {
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns false
        // not every device/emulator has access to exposure notifications Google API:
        coEvery { InternalExposureNotificationClient.asyncStart() } returns mockk()

        scenario.onFragment {
            val helper = InternalExposureNotificationPermissionHelper(it, callback)
            helper.requestPermissionToStartTracing()
        }
        assertThat(fail, `is`(false))
        assertThat(startSuccess, `is`(true))
    }

    /**
     * Test tracing permission request exception handling.
     */
    @Test
    fun testRequestPermissionToStartTracingExceptionHandling() {
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns false

        // not every device/emulator has access to exposure notifications Google API:
        coEvery { InternalExposureNotificationClient.asyncStart() } throws mockApiException(Status.RESULT_CANCELED)

        scenario.onFragment {
            val helper = InternalExposureNotificationPermissionHelper(it, callback)
            helper.requestPermissionToStartTracing()
        }
        assertThat(fail, `is`(true))
        assertThat(startSuccess, `is`(false))
    }

    /**
     * Test keys sharing permission request.
     */
    @Test
    fun testRequestPermissionToShareKeys() {
        // not every device/emulator has access to exposure notifications Google API:
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns mockk()

        scenario.onFragment {
            val helper = InternalExposureNotificationPermissionHelper(it, callback)
            helper.requestPermissionToShareKeys()
        }
        assertThat(fail, `is`(false))
        assertThat(sharingSuccess, `is`(true))
    }

    /**
     * Test keys sharing permission request exception handling.
     */
    @Test
    fun testRequestPermissionToShareKeysException() {
        // not every device/emulator has access to exposure notifications Google API:
        coEvery {
            InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory()
        } throws mockApiException(Status.RESULT_CANCELED)

        scenario.onFragment {
            val helper = InternalExposureNotificationPermissionHelper(it, callback)
            helper.requestPermissionToShareKeys()
        }
        assertThat(fail, `is`(true))
        assertThat(sharingSuccess, `is`(false))
    }

    private fun mockApiException(status: Status): ApiException {
        mockkObject(LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext()))
        val exception = ApiException(status)
        // don't need a dialog for exception
        every {
            LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext())
                .sendBroadcast(any())
        } returns true
        return exception
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
