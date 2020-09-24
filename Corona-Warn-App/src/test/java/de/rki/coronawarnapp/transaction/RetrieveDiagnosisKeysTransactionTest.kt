package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.GoogleAPIVersion
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.mockk.MockKAnnotations
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.io.File
import java.nio.file.Paths
import java.util.Date
import java.util.UUID

/**
 * RetrieveDiagnosisKeysTransaction test.
 */
class RetrieveDiagnosisKeysTransactionTest {

    @MockK
    lateinit var mockEnfClient: ENFClient

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(AppInjector)
        val appComponent = mockk<ApplicationComponent>().apply {
            every { transRetrieveKeysInjection } returns RetrieveDiagnosisInjectionHelper(
                TransactionCoroutineScope(),
                GoogleAPIVersion(),
                mockEnfClient
            )
        }
        every { AppInjector.component } returns appComponent

        mockkObject(InternalExposureNotificationClient)
        mockkObject(ApplicationConfigurationService)
        mockkObject(RetrieveDiagnosisKeysTransaction)
        mockkObject(LocalData)

        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        coEvery { ApplicationConfigurationService.asyncRetrieveExposureConfiguration() } returns mockk()
        every { LocalData.googleApiToken(any()) } just Runs
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch() } returns Date()
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch(any()) } just Runs
        every { LocalData.googleApiToken() } returns UUID.randomUUID().toString()
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `unsuccessful ENF submission`() {
        coEvery { mockEnfClient.provideDiagnosisKeys(any(), any(), any()) } returns false
        val requestedCountries = listOf("DE")
        coEvery {
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                requestedCountries
            )
        } returns listOf<File>()

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start(requestedCountries)
        }

        coVerifyOrder {
            RetrieveDiagnosisKeysTransaction["executeSetup"]()
            RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                requestedCountries
            )
        }
        coVerify(exactly = 0) {
            RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
        }
    }

    @Test
    fun `successful submission`() {
        val file = Paths.get("src", "test", "resources", "keys.bin").toFile()
        coEvery { mockEnfClient.provideDiagnosisKeys(listOf(file), any(), any()) } returns true
        val requestedCountries = listOf("DE")

        coEvery {
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                requestedCountries
            )
        } returns listOf(file)

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start(requestedCountries)
        }

        coVerifyOrder {
            RetrieveDiagnosisKeysTransaction["executeSetup"]()
            RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](
                requestedCountries
            )
            mockEnfClient.provideDiagnosisKeys(listOf(file), any(), any())
            RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
        }
    }

    @Test
    fun `conversion from date to localdate`() {
        LocalDate.fromDateFields(Date(0)) shouldBe LocalDate.parse("1970-01-01")
    }
}
