package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.security.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class SubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var submissionService: SubmissionService

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var tekHistoryStorage: TEKHistoryStorage
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool
    @MockK lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var tracingSettings: TracingSettings

    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val tan = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
    private val testResult = TestResult.PENDING
    private val registrationData = SubmissionService.RegistrationData(registrationToken, testResult)

    private val registrationTokenPreference = mockFlowPreference<String?>(null)
    private val resultReceivedTimeStamp = Instant.ofEpochMilli(101010101)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.encryptedPreferencesFactory } returns encryptedPreferencesFactory
        every { appComponent.errorResetTool } returns encryptionErrorResetTool

        every { backgroundNoise.scheduleDummyPattern() } just Runs

        every { submissionSettings.registrationToken } returns registrationTokenPreference

        every { submissionSettings.devicePairingSuccessfulAt = any() } just Runs
        every { submissionSettings.initialTestResultReceivedAt } returns resultReceivedTimeStamp

        every { submissionSettings.hasGivenConsent } returns mockFlowPreference(false)
        every { submissionSettings.hasViewedTestResult } returns mockFlowPreference(false)
        every { submissionSettings.symptoms } returns mockFlowPreference(Symptoms.NO_INFO_GIVEN)
        every { submissionSettings.clear() } just Runs

        every { taskController.tasks } returns emptyFlow()

        coEvery { tekHistoryStorage.clear() } just Runs

        every { timeStamper.nowUTC } returns Instant.EPOCH
    }

    fun createInstance(scope: CoroutineScope) = SubmissionRepository(
        scope = scope,
        submissionSettings = submissionSettings,
        submissionService = submissionService,
        timeStamper = timeStamper,
        tekHistoryStorage = tekHistoryStorage,
        deadmanNotificationScheduler = deadmanNotificationScheduler,
        backgroundNoise = backgroundNoise,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        tracingSettings = tracingSettings
    )

    @Test
    fun removeTestFromDeviceSucceeds() = runBlockingTest {
        val submissionRepository = createInstance(scope = this)

        val initialPollingForTestResultTimeStampSlot = slot<Long>()
        val isTestResultAvailableNotificationSent = slot<Boolean>()
        every {
            tracingSettings.initialPollingForTestResultTimeStamp = capture(initialPollingForTestResultTimeStampSlot)
        } answers {}
        every {
            tracingSettings.isTestResultAvailableNotificationSent = capture(isTestResultAvailableNotificationSent)
        } answers {}

        every { submissionSettings.initialTestResultReceivedAt = any() } just Runs
        every { submissionSettings.isAllowedToSubmitKeys = any() } just Runs
        every { submissionSettings.isSubmissionSuccessful = any() } just Runs
        every { analyticsKeySubmissionCollector.reset() } just Runs

        submissionRepository.removeTestFromDevice()

        verify(exactly = 1) {
            registrationTokenPreference.update(any())
            submissionSettings.devicePairingSuccessfulAt = null
            submissionSettings.initialTestResultReceivedAt = null
            submissionSettings.isAllowedToSubmitKeys = false
            submissionSettings.isSubmissionSuccessful = false
        }

        assert(initialPollingForTestResultTimeStampSlot.captured == 0L)
        assert(!isTestResultAvailableNotificationSent.captured)
    }

    @Test
    fun registrationWithGUIDSucceeds() = runBlockingTest {
        coEvery { submissionService.asyncRegisterDeviceViaGUID(guid) } returns registrationData
        coEvery { analyticsKeySubmissionCollector.reportTestRegistered() } just Runs

        val submissionRepository = createInstance(scope = this)

        submissionRepository.asyncRegisterDeviceViaGUID(guid)

        registrationTokenPreference.value shouldBe registrationToken
        submissionRepository.testResultReceivedDateFlow.first() shouldBe resultReceivedTimeStamp.toDate()

        verify(exactly = 1) {
            registrationTokenPreference.update(any())
            submissionSettings.devicePairingSuccessfulAt = any()
            backgroundNoise.scheduleDummyPattern()
        }
    }

    @Test
    fun registrationWithTeleTANSucceeds() = runBlockingTest {
        coEvery { submissionService.asyncRegisterDeviceViaTAN(tan) } returns registrationData
        coEvery { analyticsKeySubmissionCollector.reportTestRegistered() } just Runs
        every { analyticsKeySubmissionCollector.reportRegisteredWithTeleTAN() } just Runs

        val submissionRepository = createInstance(scope = this)

        submissionRepository.asyncRegisterDeviceViaTAN(tan)

        registrationTokenPreference.value shouldBe registrationToken
        submissionRepository.testResultReceivedDateFlow.first() shouldBe resultReceivedTimeStamp.toDate()

        verify(exactly = 1) {
            registrationTokenPreference.update(any())
            submissionSettings.devicePairingSuccessfulAt = any()
            backgroundNoise.scheduleDummyPattern()
        }
    }

    @Test
    fun `reset clears tek history and settings`() = runBlockingTest {
        val instance = createInstance(this)
        instance.reset()

        instance.deviceUIStateFlow.first() shouldBe NetworkRequestWrapper.RequestIdle

        coVerifyOrder {
            tekHistoryStorage.clear()
            submissionSettings.clear()
        }
    }

    @Test
    fun `ui state is SUBMITTED_FINAL when submission was done`() = runBlockingTest {
        every { submissionSettings.isSubmissionSuccessful } returns true

        val submissionRepository = createInstance(scope = this)

        submissionRepository.refreshDeviceUIState()
        submissionRepository.deviceUIStateFlow.first() shouldBe
            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL)
    }

    @Test
    fun `ui state is UNPAIRED when no token is present`() = runBlockingTest {
        every { submissionSettings.isSubmissionSuccessful } returns false
        every { submissionSettings.registrationToken } returns mockFlowPreference(null)

        val submissionRepository = createInstance(scope = this)

        submissionRepository.refreshDeviceUIState()
        submissionRepository.deviceUIStateFlow.first() shouldBe
            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED)
    }

    @Test
    fun `ui state is PAIRED_POSITIVE when allowed to submit`() = runBlockingTest {
        every { submissionSettings.isSubmissionSuccessful } returns false
        every { submissionSettings.registrationToken } returns mockFlowPreference("token")
        coEvery { submissionSettings.isAllowedToSubmitKeys } returns true

        val submissionRepository = createInstance(scope = this)

        submissionRepository.refreshDeviceUIState()
        submissionRepository.deviceUIStateFlow.first() shouldBe
            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE)
    }

    @Test
    fun `refresh when state is PAIRED_NO_RESULT`() = runBlockingTest {
        every { submissionSettings.isSubmissionSuccessful } returns false
        every { submissionSettings.registrationToken } returns mockFlowPreference("token")
        coEvery { submissionSettings.isAllowedToSubmitKeys } returns false
        coEvery { submissionService.asyncRequestTestResult(any()) } returns TestResult.PENDING

        val submissionRepository = createInstance(scope = this)

        submissionRepository.refreshDeviceUIState()
        submissionRepository.deviceUIStateFlow.first() shouldBe
            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT)

        coVerify(exactly = 1) { submissionService.asyncRequestTestResult(any()) }
    }

    @Test
    fun `refresh when state is UNPAIRED`() = runBlockingTest {
        every { submissionSettings.isSubmissionSuccessful } returns false
        every { submissionSettings.registrationToken } returns mockFlowPreference(null)
        coEvery { submissionSettings.isAllowedToSubmitKeys } returns false
        coEvery { submissionService.asyncRequestTestResult(any()) } returns TestResult.PENDING

        val submissionRepository = createInstance(scope = this)

        submissionRepository.refreshDeviceUIState()
        submissionRepository.deviceUIStateFlow.first() shouldBe
            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED)

        every { submissionSettings.registrationToken } returns mockFlowPreference("token")

        submissionRepository.refreshDeviceUIState()

        coVerify(exactly = 1) { submissionService.asyncRequestTestResult(any()) }
    }

    @Test
    fun `no refresh when state is SUBMITTED_FINAL`() = runBlockingTest {
        every { submissionSettings.isSubmissionSuccessful } returns true

        val submissionRepository = createInstance(scope = this)

        submissionRepository.refreshDeviceUIState()

        submissionRepository.deviceUIStateFlow.first() shouldBe
            NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL)

        submissionRepository.refreshDeviceUIState()

        coVerify(exactly = 0) { submissionService.asyncRequestTestResult(any()) }
    }
}
