package de.rki.coronawarnapp.transaction

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
import java.nio.file.Paths
import java.util.Date
import java.util.UUID

/**
 * RetrieveDiagnosisKeysTransaction test.
 */
class RetrieveDiagnosisKeysTransactionTest {

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
        } returns mockk()
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

        coEvery { RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf(
            file
        )

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start()

            coVerifyOrder {
                RetrieveDiagnosisKeysTransaction["executeSetup"]()
                RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
                RetrieveDiagnosisKeysTransaction["executeAPISubmission"](
                    any<String>(),
                    listOf(file),
                    any<ExposureConfiguration>()
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
