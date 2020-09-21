package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.GoogleAPIVersion
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.mockk.MockKAnnotations
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
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        every { LocalData.googleAPIProvideDiagnosisKeysCallCount = any() } just Runs
        every { LocalData.googleAPIProvideDiagnosisKeysCallCount } returns 0
        every { LocalData.nextTimeRateLimitingUnlocks = any() } just Runs
        every { LocalData.nextTimeRateLimitingUnlocks } returns Instant.now()
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `unsuccessful ENF submission`() {
        coEvery { mockEnfClient.provideDiagnosisKeys(any(), any(), any()) } returns false
        coEvery { RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf<File>()

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start()
        }

        coVerifyOrder {
            RetrieveDiagnosisKeysTransaction["executeSetup"]()
            RetrieveDiagnosisKeysTransaction["executeQuotaCalculation"]()
            RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
        }
        coVerify(exactly = 0) {
            RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
        }
    }

    @Test
    fun `successful submission`() {
        val file = Paths.get("src", "test", "resources", "keys.bin").toFile()
        coEvery { mockEnfClient.provideDiagnosisKeys(listOf(file), any(), any()) } returns true

        coEvery { RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf(
            file
        )

        runBlocking {
            RetrieveDiagnosisKeysTransaction.start()
        }

        coVerifyOrder {
            RetrieveDiagnosisKeysTransaction["executeSetup"]()
            RetrieveDiagnosisKeysTransaction["executeQuotaCalculation"]()
            RetrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
            RetrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
            mockEnfClient.provideDiagnosisKeys(listOf(file), any(), any())
            RetrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
        }
    }
}
