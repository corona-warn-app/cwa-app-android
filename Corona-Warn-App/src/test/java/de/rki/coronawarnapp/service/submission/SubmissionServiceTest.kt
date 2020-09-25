package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.formatter.TestResult
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

class SubmissionServiceTest {

    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
    private val testResult = TestResult.PENDING

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var mockPlaybook: Playbook
    @MockK lateinit var appComponent: ApplicationComponent

    private val symptoms = Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, Symptoms.Indication.POSITIVE)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent

        every { appComponent.playbook } returns mockPlaybook

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise

        mockkObject(SubmitDiagnosisKeysTransaction)
        mockkObject(LocalData)

        mockkObject(SubmissionRepository)
        every { SubmissionRepository.updateTestResult(any()) } just Runs

        every { LocalData.teletan() } returns null
        every { LocalData.testGUID() } returns null
        every { LocalData.registrationToken() } returns null
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun registerDeviceWithoutTANOrGUIDFails(): Unit = runBlocking {
        shouldThrow<NoGUIDOrTANSetException> {
            SubmissionService.asyncRegisterDevice()
        }
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
            SubmissionService.asyncRegisterDevice()
        }

        verify(exactly = 1) {
            LocalData.registrationToken(registrationToken)
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.testGUID(null)
            backgroundNoise.scheduleDummyPattern()
            SubmissionRepository.updateTestResult(testResult)
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
            SubmissionService.asyncRegisterDevice()
        }

        verify(exactly = 1) {
            LocalData.registrationToken(registrationToken)
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.teletan(null)
            backgroundNoise.scheduleDummyPattern()
            SubmissionRepository.updateTestResult(testResult)
        }
    }

    @Test
    fun requestTestResultWithoutRegistrationTokenFails(): Unit = runBlocking {
        shouldThrow<NoRegistrationTokenSetException> {
            SubmissionService.asyncRequestTestResult()
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery { mockPlaybook.testResult(registrationToken) } returns TestResult.NEGATIVE

        runBlocking {
            SubmissionService.asyncRequestTestResult() shouldBe TestResult.NEGATIVE
        }
    }

    @Test
    fun submitExposureKeysWithoutRegistrationTokenFails(): Unit = runBlocking {
        shouldThrow<NoRegistrationTokenSetException> {
            SubmissionService.asyncSubmitExposureKeys(listOf(), symptoms)
        }
    }

    @Test
    fun submitExposureKeysSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery {
            SubmitDiagnosisKeysTransaction.start(
                registrationToken,
                any(),
                symptoms
            )
        } just Runs

        runBlocking {
            SubmissionService.asyncSubmitExposureKeys(listOf(), symptoms)
        }
    }

    @Test
    fun deleteRegistrationTokenSucceeds() {
        every { LocalData.registrationToken(null) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(0L) } just Runs

        SubmissionService.deleteRegistrationToken()

        verify(exactly = 1) {
            LocalData.registrationToken(null)
            LocalData.devicePairingSuccessfulTimestamp(0L)
        }
    }
}
