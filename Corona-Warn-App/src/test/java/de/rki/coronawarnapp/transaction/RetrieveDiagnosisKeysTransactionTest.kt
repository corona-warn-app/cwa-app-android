package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.GoogleAPIVersion
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.di.Injector
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
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
        mockkObject(Injector)
        val appComponent = mockk<ApplicationComponent>().apply {
            every { transRetrieveKeysInjection } returns RetrieveDiagnosisInjectionHelper(
                TransactionCoroutineScope(),
                GoogleAPIVersion()
            )
        }
        every { Injector.component } returns appComponent

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
        coEvery {
            InternalExposureNotificationClient.getVersion()
        } returns 17000000L
        coEvery { ApplicationConfigurationService.asyncRetrieveExposureConfiguration() } returns mockk()
        every { LocalData.googleApiToken(any()) } just Runs
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch() } returns Date()
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch(any()) } just Runs
        every { LocalData.googleApiToken() } returns UUID.randomUUID().toString()
        every { LocalData.googleAPIProvideDiagnosisKeysCallCount = any() } just Runs
        every { LocalData.googleAPIProvideDiagnosisKeysCallCount } returns 0
        every { LocalData.nextTimeRateLimitingUnlocks = any() } just Runs
        every { LocalData.nextTimeRateLimitingUnlocks } returns Instant.now()
    }

    @Test
    fun testTransactionNoFiles() {
        coEvery { RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf<File>()

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start()

            coVerifyOrder {
                RetrieveDiagnosisKeysTransaction["executeSetup"]()
                RetrieveDiagnosisKeysTransaction["executeQuotaCalculation"]()
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
                RetrieveDiagnosisKeysTransaction["executeQuotaCalculation"]()
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
