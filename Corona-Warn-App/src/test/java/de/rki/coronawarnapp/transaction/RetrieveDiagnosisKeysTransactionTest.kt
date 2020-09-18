package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.GoogleAPIVersion
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
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
import org.joda.time.LocalDate
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
        mockkObject(AppInjector)
        val appComponent = mockk<ApplicationComponent>().apply {
            every { transRetrieveKeysInjection } returns RetrieveDiagnosisInjectionHelper(
                TransactionCoroutineScope(),
                GoogleAPIVersion()
            )
        }
        every { AppInjector.component } returns appComponent

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
        val requestedCountries = listOf("DE")
        coEvery {
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                requestedCountries
            )
        } returns listOf<File>()

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start(requestedCountries)

            coVerifyOrder {
                RetrieveDiagnosisKeysTransaction["executeSetup"]()
                RetrieveDiagnosisKeysTransaction["executeQuotaCalculation"]()
                RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                    requestedCountries
                )
                RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
            }
        }
    }

    @Test
    fun testTransactionHasFiles() {
        val file = Paths.get("src", "test", "resources", "keys.bin").toFile()
        val requestedCountries = listOf("DE")

        coEvery {
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                requestedCountries
            )
        } returns listOf(file)

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start(requestedCountries)

            coVerifyOrder {
                RetrieveDiagnosisKeysTransaction["executeSetup"]()
                RetrieveDiagnosisKeysTransaction["executeQuotaCalculation"]()
                RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                    requestedCountries
                )
                RetrieveDiagnosisKeysTransaction["executeAPISubmission"](
                    any<String>(),
                    listOf(file),
                    any<ExposureConfiguration>()
                )
                RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
            }
        }
    }

    @Test
    fun `conversion from date to localdate`() {
        LocalDate.fromDateFields(Date(0)) shouldBe LocalDate.parse("1970-01-01")
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
