package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.security.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.verification.server.VerificationKeyType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.preferences.mockFlowPreference

class SubmissionServiceTest {

    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
    private val testResult = TestResult.PENDING

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var mockPlaybook: Playbook
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool

    lateinit var submissionRepository: SubmissionRepository

    private val symptoms = Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, Symptoms.Indication.POSITIVE)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent

        every { appComponent.playbook } returns mockPlaybook
        every { appComponent.encryptedPreferencesFactory } returns encryptedPreferencesFactory
        every { appComponent.errorResetTool } returns encryptionErrorResetTool

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise

        mockkObject(LocalData)

        every { LocalData.teletan() } returns null
        every { LocalData.testGUID() } returns null
        every { LocalData.registrationToken() } returns null

        every {submissionSettings.hasGivenConsent } returns mockFlowPreference(false)

        submissionRepository = SubmissionRepository(submissionSettings, mockPlaybook)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun registrationWithGUIDSucceeds() {
        every { LocalData.testGUID() } returns guid

        every { LocalData.testGUID(any()) } just Runs
        every { LocalData.registrationToken(any()) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs

        coEvery {
            mockPlaybook.initialRegistration(any(), VerificationKeyType.GUID)
        } returns (registrationToken to TestResult.PENDING)
        coEvery { mockPlaybook.testResult(registrationToken) } returns testResult

        every { backgroundNoise.scheduleDummyPattern() } just Runs

        runBlocking {
            submissionRepository.asyncRegisterDeviceViaGUID(guid)
        }

        verify(exactly = 1) {
            LocalData.registrationToken(registrationToken)
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.testGUID(null)
            backgroundNoise.scheduleDummyPattern()
            submissionRepository.updateTestResult(testResult)
        }
    }

    @Test
    fun registrationWithTeleTANSucceeds() {
        every { LocalData.teletan() } returns guid

        every { LocalData.teletan(any()) } just Runs
        every { LocalData.registrationToken(any()) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs

        coEvery {
            mockPlaybook.initialRegistration(any(), VerificationKeyType.TELETAN)
        } returns (registrationToken to TestResult.PENDING)
        coEvery { mockPlaybook.testResult(registrationToken) } returns testResult

        every { backgroundNoise.scheduleDummyPattern() } just Runs

        runBlocking {
            submissionRepository.asyncRegisterDeviceViaTAN(guid)
        }

        verify(exactly = 1) {
            LocalData.registrationToken(registrationToken)
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.teletan(null)
            backgroundNoise.scheduleDummyPattern()
            submissionRepository.updateTestResult(testResult)
        }
    }

    @Test
    fun requestTestResultWithoutRegistrationTokenFails(): Unit = runBlocking {
        shouldThrow<NoRegistrationTokenSetException> {
            submissionRepository.asyncRequestTestResult()
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery { mockPlaybook.testResult(registrationToken) } returns TestResult.NEGATIVE

        runBlocking {
            submissionRepository.asyncRequestTestResult() shouldBe TestResult.NEGATIVE
        }
    }

    @Test
    fun deleteRegistrationTokenSucceeds() {
        every { LocalData.registrationToken(null) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(0L) } just Runs

        SubmissionRepository.deleteRegistrationToken()

        verify(exactly = 1) {
            LocalData.registrationToken(null)
            LocalData.devicePairingSuccessfulTimestamp(0L)
        }
    }
}
