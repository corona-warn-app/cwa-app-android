package de.rki.coronawarnapp.transaction

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.RuntimeException
import java.nio.file.Paths
import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * RetrieveDiagnosisKeysTransaction test.
 */
class RetrieveDiagnosisKeysTransactionTest {

    private val callCount = AtomicInteger(0)
    private val succeedThrowSequence = listOf(true, false, true)

    private suspend fun asyncProvideDiagnosisKeysThrowing(): Void = suspendCoroutine { cont ->
        val currentCallCount = callCount.getAndIncrement()
        if (currentCallCount >= succeedThrowSequence.size) {
            throw RuntimeException("call count exceeded provided sequence")
        }
        if (succeedThrowSequence[currentCallCount]) {
            cont.resume(mockk())
        } else {
            cont.resumeWithException(ApiException(Status.RESULT_INTERNAL_ERROR))
        }
    }

    @Before
    fun setUp() {
        mockkObject(InternalExposureNotificationClient)
        mockkObject(ApplicationConfigurationService)
        mockkObject(RetrieveDiagnosisKeysTransaction)
        mockkObject(LocalData)

        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true
        coEvery {
            InternalExposureNotificationClient.asyncProvideDiagnosisKeys(
                any(),
                any(),
                any()
            )
        } coAnswers { asyncProvideDiagnosisKeysThrowing() }
        coEvery { ApplicationConfigurationService.asyncRetrieveExposureConfiguration() } returns mockk()
        every { LocalData.googleApiToken(any()) } just Runs
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch() } returns Date()
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch(any()) } just Runs
        every { LocalData.googleApiToken() } returns UUID.randomUUID().toString()
    }

    @Test
    fun testTransactionNoFiles() {
        coEvery { RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf<File>()

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start()

            coVerifyOrder {
                RetrieveDiagnosisKeysTransaction["executeSetup"]()
                RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
                RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
            }
        }
    }

    @Test
    fun testTransactionHasFiles() {
        val file = Paths.get("src", "test", "resources", "keys.bin").toFile()
        val batch = listOf(file, file)

        coEvery { RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns batch

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start()

            coVerifyOrder {
                RetrieveDiagnosisKeysTransaction["executeSetup"]()
                RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
                RetrieveDiagnosisKeysTransaction["executeAPISubmission"](
                    any<String>(),
                    batch,
                    any<ExposureConfiguration>()
                )

                // executing first file - should succeed
                RetrieveDiagnosisKeysTransaction["executeSingleElementBatch"](
                    file,
                    any<ExposureConfiguration>(),
                    any<String>(),
                    any<Int>()
                )

                // succeeds
                InternalExposureNotificationClient["asyncProvideDiagnosisKeys"](
                    listOf(file),
                    any<ExposureConfiguration>(),
                    any<String>()
                )

                // executing second file - should fail and then succeed
                RetrieveDiagnosisKeysTransaction["executeSingleElementBatch"](
                    file,
                    any<ExposureConfiguration>(),
                    any<String>(),
                    any<Int>()
                )

                // throws
                InternalExposureNotificationClient["asyncProvideDiagnosisKeys"](
                    listOf(file),
                    any<ExposureConfiguration>(),
                    any<String>()
                )

                // succeeds
                InternalExposureNotificationClient["asyncProvideDiagnosisKeys"](
                    listOf(file),
                    any<ExposureConfiguration>(),
                    any<String>()
                )

                RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
