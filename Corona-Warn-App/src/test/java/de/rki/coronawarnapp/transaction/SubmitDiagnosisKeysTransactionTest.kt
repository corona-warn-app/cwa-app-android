package de.rki.coronawarnapp.transaction

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.http.responses.TanResponse
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.LocalData
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class SubmitDiagnosisKeysTransactionTest {
    private val authString = "authString"

    private lateinit var submitDiagnosisKeysTransaction: SubmitDiagnosisKeysTransaction

    @MockK
    private lateinit var submissionService: SubmissionService

    @MockK
    private lateinit var verificationService: VerificationService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        mockkObject(InternalExposureNotificationClient)
        every { LocalData.numberOfSuccessfulSubmissions(any()) } just Runs
        coEvery { verificationService.getTAN(any(), any(), any()) } returns TanResponse(authString)

        submitDiagnosisKeysTransaction =
            SubmitDiagnosisKeysTransaction(submissionService, verificationService)
    }

    @Test
    fun testTransactionNoKeys() {
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns listOf()
        coEvery { submissionService.submitKeys(any(), authString, any(), any()) } returns mockk()

        runBlocking {
            submitDiagnosisKeysTransaction.start("123", listOf())

            coVerifyOrder {
                submissionService.submitKeys(any(), authString, any(), any())
                LocalData.numberOfSuccessfulSubmissions(1)
            }
        }
    }

    @Test
    fun testTransactionHasKeys() {
        val key = TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setKeyData(ByteArray(1))
            .setRollingPeriod(1)
            .setRollingStartIntervalNumber(1)
            .setTransmissionRiskLevel(1)
            .build()
        val testList = slot<KeyExportFormat.SubmissionPayload>()
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns listOf(
            key
        )
        coEvery {
            submissionService.submitKeys(
                any(),
                authString,
                any(),
                capture(testList)
            )
        } returns mockk()

        runBlocking {
            submitDiagnosisKeysTransaction.start("123", listOf(key))

            coVerifyOrder {
                submissionService.submitKeys(any(), authString, any(), any())
                LocalData.numberOfSuccessfulSubmissions(1)
            }
            assertThat(testList.isCaptured, `is`(true))
            assertThat(testList.captured.keysList.size, `is`(1))
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
