package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.coroutine.AppCoroutineScope
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.security.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.preferences.mockFlowPreference

class SubmissionRepositoryTest {

    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var submissionService: SubmissionService

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var appComponent: ApplicationComponent

    @MockK lateinit var encryptedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var encryptionErrorResetTool: EncryptionErrorResetTool

    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val tan = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
    private val testResult = TestResult.PENDING
    private val registrationData = SubmissionService.RegistrationData(registrationToken, testResult)

    lateinit var submissionRepository: SubmissionRepository

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
        every { LocalData.devicePairingSuccessfulTimestamp(0L) } just Runs
        every { LocalData.initialTestResultReceivedTimestamp() } returns 1L
        every { LocalData.registrationToken(any()) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs

        every {submissionSettings.hasGivenConsent } returns mockFlowPreference(false)

        val appScope =  AppCoroutineScope()
        submissionRepository = SubmissionRepository(submissionSettings, submissionService, appScope)
    }

    @Test
    fun deleteRegistrationTokenSucceeds() {
        SubmissionRepository.deleteRegistrationToken()

        verify(exactly = 1) {
            LocalData.registrationToken(null)
            LocalData.devicePairingSuccessfulTimestamp(0L)
        }
    }

    @Test
    fun registrationWithGUIDSucceeds() {
        every { LocalData.testGUID(any()) } just Runs
        coEvery { submissionService.asyncRegisterDeviceViaGUID(guid) } returns registrationData

        runBlocking {
            submissionRepository.asyncRegisterDeviceViaGUID(guid)
        }

        verify(exactly = 1) {
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.registrationToken(registrationToken)
            LocalData.testGUID(null)
            backgroundNoise.scheduleDummyPattern()
            submissionRepository.updateTestResult(testResult)
        }
    }
    @Test
    fun registrationWithTeleTANSucceeds() {
        every { LocalData.teletan(any()) } just Runs
        coEvery { submissionService.asyncRegisterDeviceViaTAN(tan) } returns registrationData

        runBlocking {
            submissionRepository.asyncRegisterDeviceViaTAN(tan)
        }

        coVerify(exactly = 1) {
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.registrationToken(registrationToken)
            LocalData.teletan(null)
            backgroundNoise.scheduleDummyPattern()
            submissionRepository.updateTestResult(testResult)
        }
    }
}
