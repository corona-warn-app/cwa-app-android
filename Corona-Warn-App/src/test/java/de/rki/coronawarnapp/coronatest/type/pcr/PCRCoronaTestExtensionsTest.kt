package de.rki.coronawarnapp.coronatest.type.pcr

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PCRCoronaTestExtensionsTest : BaseTest() {

    @Test
    fun `state determination, unregistered test`() = runBlockingTest {
        val test: PCRCoronaTest? = null
        test.toSubmissionState() shouldBe SubmissionStatePCR.NoTest
    }

//     @Test
//    fun removeTestFromDeviceSucceeds() = runBlockingTest {
//        val submissionRepository = createInstance(scope = this)
//
//        val initialPollingForTestResultTimeStampSlot = slot<Long>()
//        val isTestResultAvailableNotificationSent = slot<Boolean>()
//        every {
//            tracingSettings.initialPollingForTestResultTimeStamp = capture(initialPollingForTestResultTimeStampSlot)
//        } answers {}
//        every {
//            tracingSettings.isTestResultAvailableNotificationSent = capture(isTestResultAvailableNotificationSent)
//        } answers {}
//
//        every { submissionSettings.isAllowedToSubmitKeys = any() } just Runs
//        every { submissionSettings.isSubmissionSuccessful = any() } just Runs
//
//        submissionRepository.removeTestFromDevice()
//
//        verify(exactly = 1) {
//            testResultDataCollector.clear()
//            registrationTokenPreference.update(any())
//            submissionSettings.devicePairingSuccessfulAt = null
//            submissionSettings.initialTestResultReceivedAt = null
//            submissionSettings.isAllowedToSubmitKeys = false
//            submissionSettings.isSubmissionSuccessful = false
//        }
//
//        initialPollingForTestResultTimeStampSlot.captured shouldBe 0L
//        isTestResultAvailableNotificationSent.captured shouldBe false
//    }
//
//    @Test
//    fun registrationWithGUIDSucceeds() = runBlockingTest {
//        coEvery { submissionService.asyncRegisterDeviceViaGUID(guid) } returns registrationData
//        coEvery { analyticsKeySubmissionCollector.reportTestRegistered() } just Runs
//        every { analyticsKeySubmissionCollector.reset() } just Runs
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.asyncRegisterDeviceViaGUID(guid)
//
//        registrationTokenPreference.value shouldBe registrationToken
//        submissionRepository.testResultReceivedDateFlow.first() shouldBe resultReceivedTimeStamp.toDate()
//
//        verify(exactly = 1) {
//            registrationTokenPreference.update(any())
//            submissionSettings.devicePairingSuccessfulAt = any()
//            backgroundNoise.scheduleDummyPattern()
//        }
//
//        coVerify { testResultDataCollector.saveTestResultAnalyticsSettings(any()) }
//    }
//
//    @Test
//    fun registrationWithTeleTANSucceeds() = runBlockingTest {
//        coEvery { submissionService.asyncRegisterDeviceViaTAN(tan) } returns registrationData
//        coEvery { analyticsKeySubmissionCollector.reportTestRegistered() } just Runs
//        every { analyticsKeySubmissionCollector.reportRegisteredWithTeleTAN() } just Runs
//        every { analyticsKeySubmissionCollector.reset() } just Runs
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.asyncRegisterDeviceViaTAN(tan)
//
//        registrationTokenPreference.value shouldBe registrationToken
//        submissionRepository.testResultReceivedDateFlow.first() shouldBe resultReceivedTimeStamp.toDate()
//
//        verify(exactly = 1) {
//            registrationTokenPreference.update(any())
//            submissionSettings.devicePairingSuccessfulAt = any()
//            backgroundNoise.scheduleDummyPattern()
//        }
//
//        coVerify(exactly = 0) {
//            testResultDataCollector.saveTestResultAnalyticsSettings(any())
//        }
//    }
//
//    @Test
//    fun `reset clears tek history and settings`() = runBlockingTest {
//        val instance = createInstance(this)
//        instance.reset()
//
//        instance.deviceUIStateFlow.first() shouldBe NetworkRequestWrapper.RequestIdle
//
//        coVerifyOrder {
//            tekHistoryStorage.clear()
//            submissionSettings.clear()
//        }
//    }
//
//    @Test
//    fun `ui state is SUBMITTED_FINAL when submission was done`() = runBlockingTest {
//        every { submissionSettings.isSubmissionSuccessful } returns true
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.refreshTest()
//        submissionRepository.deviceUIStateFlow.first() shouldBe
//            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL)
//    }
//
//    @Test
//    fun `ui state is UNPAIRED when no token is present`() = runBlockingTest {
//        every { submissionSettings.isSubmissionSuccessful } returns false
//        every { submissionSettings.registrationToken } returns mockFlowPreference(null)
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.refreshTest()
//        submissionRepository.deviceUIStateFlow.first() shouldBe
//            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED)
//    }
//
//    @Test
//    fun `ui state is PAIRED_POSITIVE when allowed to submit`() = runBlockingTest {
//        every { submissionSettings.isSubmissionSuccessful } returns false
//        every { submissionSettings.registrationToken } returns mockFlowPreference("token")
//        coEvery { submissionSettings.isAllowedToSubmitKeys } returns true
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.refreshTest()
//        submissionRepository.deviceUIStateFlow.first() shouldBe
//            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE)
//    }
//
//    @Test
//    fun `refresh when state is PAIRED_NO_RESULT`() = runBlockingTest {
//        every { submissionSettings.isSubmissionSuccessful } returns false
//        every { submissionSettings.registrationToken } returns mockFlowPreference("token")
//        coEvery { submissionSettings.isAllowedToSubmitKeys } returns false
//        coEvery { submissionService.asyncRequestTestResult(any()) } returns CoronaTestResult.PCR_OR_RAT_PENDING
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.refreshTest()
//        submissionRepository.deviceUIStateFlow.first() shouldBe
//            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT)
//
//        coVerify(exactly = 1) { submissionService.asyncRequestTestResult(any()) }
//    }
//
//    @Test
//    fun `refresh when state is UNPAIRED`() = runBlockingTest {
//        every { submissionSettings.isSubmissionSuccessful } returns false
//        every { submissionSettings.registrationToken } returns mockFlowPreference(null)
//        coEvery { submissionSettings.isAllowedToSubmitKeys } returns false
//        coEvery { submissionService.asyncRequestTestResult(any()) } returns CoronaTestResult.PCR_OR_RAT_PENDING
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.refreshTest()
//        submissionRepository.deviceUIStateFlow.first() shouldBe
//            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED)
//
//        every { submissionSettings.registrationToken } returns mockFlowPreference("token")
//
//        submissionRepository.refreshTest()
//
//        coVerify(exactly = 1) { submissionService.asyncRequestTestResult(any()) }
//    }
//
//    @Test
//    fun `no refresh when state is SUBMITTED_FINAL`() = runBlockingTest {
//        every { submissionSettings.isSubmissionSuccessful } returns true
//
//        val submissionRepository = createInstance(scope = this)
//
//        submissionRepository.refreshTest()
//
//        submissionRepository.deviceUIStateFlow.first() shouldBe
//            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL)
//
//        submissionRepository.refreshTest()
//
//        coVerify(exactly = 0) { submissionService.asyncRequestTestResult(any()) }
//    }
//        @Test
//    fun `updateTestResult updates test result donor data`() = runBlockingTest {
//        val submissionRepository = createInstance(scope = this)
//        submissionRepository.updateTestResult(CoronaTestResult.PCR_NEGATIVE)
//
//        verify { testResultDataCollector.updatePendingTestResultReceivedTime(any()) }
//    }

//    @Test
//    fun `doDeviceRegistration calls TestResultDataCollector`() {
//        val viewModel = createViewModel()
//        val mockResult = mockk<QRScanResult>().apply {
//            every { guid } returns "guid"
//        }
//
//        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns CoronaTestResult.PCR_POSITIVE
//        viewModel.doDeviceRegistration(mockResult)
//    }

}
