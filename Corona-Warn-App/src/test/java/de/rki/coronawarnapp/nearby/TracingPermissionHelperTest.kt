package de.rki.coronawarnapp.nearby

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TracingPermissionHelperTest : BaseTest() {

    @Test
    fun todo() {
        TODO()
    }

//     /**
//     * Launch test fragment, required for view lifecycle owner.
//     *
//     * @see [InternalExposureNotificationPermissionHelper]
//     * @see [Fragment.getViewLifecycleOwner]
//     */
//    @Before
//    fun setUp() {
//        fail = false
//        startSuccess = false
//        sharingSuccess = false
//        mockkObject(InternalExposureNotificationClient)
//        scenario = launchFragmentInContainer<TestFragment>()
//    }
//
//    /**
//     * Test tracing permission request assuming EN Client is enabled.
//     */
//    @Test
//    fun testRequestPermissionToStartTracingENIsEnabled() {
//        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true
//        scenario.onFragment {
//            val helper = InternalExposureNotificationPermissionHelper(it, callback)
//            helper.requestPermissionToStartTracing()
//        }
//        assertThat(fail, `is`(false))
//        assertThat(startSuccess, `is`(true))
//    }
//
//    /**
//     * Test tracing permission request assuming EN Client is disabled.
//     */
//    @Test
//    fun testRequestPermissionToStartTracingENIsNotEnabled() {
//        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns false
//        // not every device/emulator has access to exposure notifications Google API:
//        coEvery { InternalExposureNotificationClient.asyncStart() } returns mockk()
//
//        scenario.onFragment {
//            val helper = InternalExposureNotificationPermissionHelper(it, callback)
//            helper.requestPermissionToStartTracing()
//        }
//        assertThat(fail, `is`(false))
//        assertThat(startSuccess, `is`(true))
//    }
//
//    /**
//     * Test tracing permission request exception handling.
//     */
//    @Test
//    fun testRequestPermissionToStartTracingExceptionHandling() {
//        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns false
//
//        // not every device/emulator has access to exposure notifications Google API:
//        coEvery { InternalExposureNotificationClient.asyncStart() } throws mockApiException(Status.RESULT_CANCELED)
//
//        scenario.onFragment {
//            val helper = InternalExposureNotificationPermissionHelper(it, callback)
//            helper.requestPermissionToStartTracing()
//        }
//        assertThat(fail, `is`(true))
//        assertThat(startSuccess, `is`(false))
//    }
//
//    /**
//     * Test keys sharing permission request.
//     */
//    @Test
//    fun testRequestPermissionToShareKeys() {
//        // not every device/emulator has access to exposure notifications Google API:
//        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns mockk()
//
//        scenario.onFragment {
//            val helper = InternalExposureNotificationPermissionHelper(it, callback)
//            helper.requestPermissionToShareKeys()
//        }
//        assertThat(fail, `is`(false))
//        assertThat(sharingSuccess, `is`(true))
//    }
//
//    /**
//     * Test keys sharing permission request exception handling.
//     */
//    @Test
//    fun testRequestPermissionToShareKeysException() {
//        // not every device/emulator has access to exposure notifications Google API:
//        coEvery {
//            InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory()
//        } throws mockApiException(Status.RESULT_CANCELED)
//
//        scenario.onFragment {
//            val helper = InternalExposureNotificationPermissionHelper(it, callback)
//            helper.requestPermissionToShareKeys()
//        }
//        assertThat(fail, `is`(true))
//        assertThat(sharingSuccess, `is`(false))
//    }
//
//    private fun mockApiException(status: Status): ApiException {
//        mockkObject(LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext()))
//        val exception = ApiException(status)
//        // don't need a dialog for exception
//        every {
//            LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext())
//                .sendBroadcast(any())
//        } returns true
//        return exception
//    }
//
//    @After
//    fun cleanUp() {
//        unmockkAll()
//    }

}
