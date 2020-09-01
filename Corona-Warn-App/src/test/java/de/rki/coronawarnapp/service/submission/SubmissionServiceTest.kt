package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.NoGUIDOrTANSetException
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
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
    private val testResult = TestResult.PENDING

    @MockK
    private lateinit var webRequestBuilder: WebRequestBuilder

    @MockK
    private lateinit var backgroundNoise: BackgroundNoise

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(WebRequestBuilder.Companion)
        every { WebRequestBuilder.getInstance() } returns webRequestBuilder

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

    @Test(expected = NoGUIDOrTANSetException::class)
    fun registerDeviceWithoutTANOrGUIDFails() {
        runBlocking {
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
            webRequestBuilder.asyncGetRegistrationToken(any(), KeyType.GUID)
        } returns registrationToken
        coEvery { webRequestBuilder.asyncGetTestResult(registrationToken) } returns testResult.value

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
            webRequestBuilder.asyncGetRegistrationToken(any(), KeyType.TELETAN)
        } returns registrationToken
        coEvery { webRequestBuilder.asyncGetTestResult(registrationToken) } returns testResult.value

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

    @Test(expected = NoRegistrationTokenSetException::class)
    fun requestTestResultWithoutRegistrationTokenFails() {
        runBlocking {
            SubmissionService.asyncRequestTestResult()
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery { webRequestBuilder.asyncGetTestResult(registrationToken) } returns TestResult.NEGATIVE.value

        runBlocking {
            assertThat(SubmissionService.asyncRequestTestResult(), equalTo(TestResult.NEGATIVE))
        }
    }

    @Test(expected = NoRegistrationTokenSetException::class)
    fun submitExposureKeysWithoutRegistrationTokenFails() {
        runBlocking {
            SubmissionService.asyncSubmitExposureKeys(listOf(), false, listOf())
        }
    }

    @Test
    fun submitExposureKeysSucceeds() {
        every { LocalData.registrationToken() } returns registrationToken
        coEvery {
            SubmitDiagnosisKeysTransaction.start(
                registrationToken,
                any(),
                any(),
                any()
            )
        } just Runs

        runBlocking {
            SubmissionService.asyncSubmitExposureKeys(listOf(), false, listOf())
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

    @Test
    fun containsValidGUID() {
        // valid
        assertThat(
            SubmissionService.containsValidGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(true)
        )

        // invalid
        assertThat(
            SubmissionService.containsValidGUID("https://no-guid-here"),
            equalTo(false)
        )
    }

    @Test
    fun extractGUID() {
        assertThat(
            SubmissionService.extractGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(guid)
        )
    }
}
