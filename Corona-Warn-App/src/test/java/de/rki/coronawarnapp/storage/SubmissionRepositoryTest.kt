package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.task.TaskController
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
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.preferences.mockFlowPreference

class SubmissionRepositoryTest {

    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var submissionService: SubmissionService

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var tekHistoryStorage: TEKHistoryStorage
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool

    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val tan = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
    private val testResult = TestResult.PENDING
    private val registrationData = SubmissionService.RegistrationData(registrationToken, testResult)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.encryptedPreferencesFactory } returns encryptedPreferencesFactory
        every { appComponent.errorResetTool } returns encryptionErrorResetTool

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise
        every { backgroundNoise.scheduleDummyPattern() } just Runs

        mockkObject(LocalData)
        every { LocalData.registrationToken(any()) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs
        every { LocalData.initialTestResultReceivedTimestamp() } returns 1L

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
        tekHistoryStorage = tekHistoryStorage
    )

    @Test
    fun removeTestFromDeviceSucceeds() = runBlockingTest {
        val submissionRepository = createInstance(scope = this)

        every { LocalData.initialPollingForTestResultTimeStamp(any()) } just Runs
        every { LocalData.initialTestResultReceivedTimestamp(any()) } just Runs
        every { LocalData.isAllowedToSubmitDiagnosisKeys(any()) } just Runs
        every { LocalData.isTestResultAvailableNotificationSent(any()) } just Runs

        submissionRepository.removeTestFromDevice()

        verify(exactly = 1) {
            LocalData.registrationToken(null)
            LocalData.devicePairingSuccessfulTimestamp(0L)
            LocalData.initialPollingForTestResultTimeStamp(0L)
            LocalData.initialTestResultReceivedTimestamp(0L)
            LocalData.isAllowedToSubmitDiagnosisKeys(false)
            LocalData.isTestResultAvailableNotificationSent(false)
        }
    }

    @Test
    fun registrationWithGUIDSucceeds() = runBlockingTest {
        coEvery { submissionService.asyncRegisterDeviceViaGUID(guid) } returns registrationData

        val submissionRepository = createInstance(scope = this)

        submissionRepository.asyncRegisterDeviceViaGUID(guid)

        verify(exactly = 1) {
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.registrationToken(registrationToken)
            backgroundNoise.scheduleDummyPattern()
            submissionRepository.updateTestResult(testResult)
        }
    }

    @Test
    fun registrationWithTeleTANSucceeds() = runBlockingTest {
        coEvery { submissionService.asyncRegisterDeviceViaTAN(tan) } returns registrationData

        val submissionRepository = createInstance(scope = this)

        submissionRepository.asyncRegisterDeviceViaTAN(tan)

        coVerify(exactly = 1) {
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.registrationToken(registrationToken)
            backgroundNoise.scheduleDummyPattern()
            submissionRepository.updateTestResult(testResult)
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
}
