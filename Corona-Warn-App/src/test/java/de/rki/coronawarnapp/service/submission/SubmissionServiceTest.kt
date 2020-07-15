package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.requests.RegistrationRequest
import de.rki.coronawarnapp.http.responses.RegistrationTokenResponse
import de.rki.coronawarnapp.http.responses.TestResultResponse
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.formatter.TestResult
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class SubmissionServiceTest {
    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"

    @MockK
    private lateinit var submitDiagnosisKeysTransaction: SubmitDiagnosisKeysTransaction

    @MockK
    private lateinit var verificationService: VerificationService

    private lateinit var submissionService: SubmissionService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)

        submissionService = SubmissionService(submitDiagnosisKeysTransaction, verificationService)

        every { LocalData.teletan() } returns null
        every { LocalData.testGUID() } returns null
        every { LocalData.registrationToken() } returns null
    }

    @Test(expected = NoGUIDOrTANSetException::class)
    fun registerDeviceWithoutTANOrGUIDFails() {
        runBlocking {
            submissionService.asyncRegisterDevice()
        }
    }

    @Test
    fun registrationWithGUIDSucceeds() {
        every { LocalData.testGUID() } returns guid

        every { LocalData.testGUID(any()) } just Runs
        every { LocalData.registrationToken(any()) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs

        coEvery {
            verificationService.getRegistrationToken(
                any(),
                any(),
                match { it.keyType == SubmissionConstants.QR_CODE_KEY_TYPE }
            )
        } returns RegistrationTokenResponse(registrationToken)

        runBlocking {
            submissionService.asyncRegisterDevice()
        }

        verify(exactly = 1) {
            LocalData.registrationToken(registrationToken)
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.testGUID(null)
        }
    }

    @Test
    fun registrationWithTeleTANSucceeds() {
        every { LocalData.teletan() } returns guid

        every { LocalData.teletan(any()) } just Runs
        every { LocalData.registrationToken(any()) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs

        coEvery {
            verificationService.getRegistrationToken(
                any(),
                any(),
                match { it.keyType == SubmissionConstants.TELE_TAN_KEY_TYPE }
            )
        } returns RegistrationTokenResponse(registrationToken)

        runBlocking {
            submissionService.asyncRegisterDevice()
        }

        verify(exactly = 1) {
            LocalData.registrationToken(registrationToken)
            LocalData.devicePairingSuccessfulTimestamp(any())
            LocalData.teletan(null)
        }
    }

    @Test(expected = NoRegistrationTokenSetException::class)
    fun requestTestResultWithoutRegistrationTokenFails() {
        runBlocking {
            submissionService.asyncRequestTestResult()
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery {
            verificationService.getTestResult(any(), any(), RegistrationRequest(registrationToken))
        } returns TestResultResponse(TestResult.NEGATIVE.value)

        runBlocking {
            assertThat(submissionService.asyncRequestTestResult(), equalTo(TestResult.NEGATIVE))
        }
    }

    @Test(expected = NoRegistrationTokenSetException::class)
    fun submitExposureKeysWithoutRegistrationTokenFails() {
        runBlocking {
            submissionService.asyncSubmitExposureKeys(listOf())
        }
    }

    @Test
    fun submitExposureKeysSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery { submitDiagnosisKeysTransaction.start(registrationToken, any()) } just Runs

        runBlocking {
            submissionService.asyncSubmitExposureKeys(listOf())
        }
    }

    @Test
    fun deleteRegistrationTokenSucceeds() {
        every { LocalData.registrationToken(null) } just Runs
        every { LocalData.devicePairingSuccessfulTimestamp(0L) } just Runs

        submissionService.deleteRegistrationToken()

        verify(exactly = 1) {
            LocalData.registrationToken(null)
            LocalData.devicePairingSuccessfulTimestamp(0L)
        }
    }

    @Test
    fun containsValidGUID() {
        // valid
        assertThat(
            submissionService.containsValidGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(true)
        )

        // invalid
        assertThat(
            submissionService.containsValidGUID("https://no-guid-here"),
            equalTo(false)
        )
    }

    @Test
    fun extractGUID() {
        assertThat(
            submissionService.extractGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(guid)
        )
    }
}
