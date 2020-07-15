package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.CachedKeyFileHolder
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
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

    @MockK
    lateinit var applicationConfigurationService: ApplicationConfigurationService

    @MockK
    lateinit var cachedKeyFileHolder: CachedKeyFileHolder

    private lateinit var retrieveDiagnosisKeysTransaction: RetrieveDiagnosisKeysTransaction

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(InternalExposureNotificationClient)
        mockkObject(LocalData)

        retrieveDiagnosisKeysTransaction =
            spyk(
                RetrieveDiagnosisKeysTransaction(
                    applicationConfigurationService,
                    cachedKeyFileHolder
                )
            )

        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true
        coEvery {
            InternalExposureNotificationClient.asyncProvideDiagnosisKeys(
                any(),
                any(),
                any()
            )
        } returns mockk()
        coEvery { applicationConfigurationService.asyncRetrieveExposureConfiguration() } returns mockk()
        every { LocalData.googleApiToken(any()) } just Runs
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch() } returns Date()
        every { LocalData.lastTimeDiagnosisKeysFromServerFetch(any()) } just Runs
        every { LocalData.googleApiToken() } returns UUID.randomUUID().toString()
    }

    @Test
    fun testTransactionNoFiles() {
        coEvery { retrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf<File>()

        runBlocking {
            retrieveDiagnosisKeysTransaction.start()

            coVerifyOrder {
                retrieveDiagnosisKeysTransaction["executeSetup"]()
                retrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                retrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
                retrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
            }
        }
    }

    @Test
    fun testTransactionHasFiles() {
        val file = Paths.get("src", "test", "resources", "keys.bin").toFile()

        coEvery { retrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>()) } returns listOf(
            file
        )

        runBlocking {
            retrieveDiagnosisKeysTransaction.start()

            coVerifyOrder {
                retrieveDiagnosisKeysTransaction["executeSetup"]()
                retrieveDiagnosisKeysTransaction["executeRetrieveRiskScoreParams"]()
                retrieveDiagnosisKeysTransaction["executeFetchKeyFilesFromServer"](any<Date>())
                retrieveDiagnosisKeysTransaction["executeAPISubmission"](
                    any<String>(),
                    listOf(file),
                    any<ExposureConfiguration>()
                )
                retrieveDiagnosisKeysTransaction["executeFetchDateUpdate"](any<Date>())
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
