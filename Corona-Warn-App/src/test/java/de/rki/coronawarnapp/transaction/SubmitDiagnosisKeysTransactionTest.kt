package de.rki.coronawarnapp.transaction

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
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

    @MockK
    private lateinit var webRequestBuilder: WebRequestBuilder

    @MockK
    private lateinit var backgroundNoise: BackgroundNoise

    private val authString = "authString"
    private val registrationToken = "123"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        mockkObject(WebRequestBuilder.Companion)
        every { WebRequestBuilder.getInstance() } returns webRequestBuilder

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise

        mockkObject(LocalData)
        mockkObject(SubmissionService)
        mockkObject(InternalExposureNotificationClient)
        mockkObject(BackgroundWorkScheduler)
        every { BackgroundWorkScheduler.stopWorkScheduler() } just Runs
        every { LocalData.numberOfSuccessfulSubmissions(any()) } just Runs
        coEvery { webRequestBuilder.asyncGetTan(registrationToken) } returns authString
    }

    @Test
    fun testTransactionNoKeys() {
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns listOf()
        coEvery { webRequestBuilder.asyncSubmitKeysToServer(authString, listOf()) } just Runs

        runBlocking {
            SubmitDiagnosisKeysTransaction.start(registrationToken, listOf())

            coVerifyOrder {
                webRequestBuilder.asyncSubmitKeysToServer(authString, listOf())
                SubmissionService.submissionSuccessful()
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
        val testList = slot<List<KeyExportFormat.TemporaryExposureKey>>()
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns listOf(
            key
        )
        coEvery {
            webRequestBuilder.asyncSubmitKeysToServer(authString, capture(testList))
        } just Runs

        runBlocking {
            SubmitDiagnosisKeysTransaction.start(registrationToken, listOf(key))

            coVerifyOrder {
                webRequestBuilder.asyncSubmitKeysToServer(authString, any())
                SubmissionService.submissionSuccessful()
            }
            assertThat(testList.isCaptured, `is`(true))
            assertThat(testList.captured.size, `is`(1))
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
