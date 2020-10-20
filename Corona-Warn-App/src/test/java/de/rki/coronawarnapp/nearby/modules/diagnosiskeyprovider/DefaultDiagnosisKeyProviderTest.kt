@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.util.GoogleAPIVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask
import java.io.File

class DefaultDiagnosisKeyProviderTest : BaseTest() {
    @MockK
    lateinit var googleENFClient: ExposureNotificationClient

    @MockK
    lateinit var googleAPIVersion: GoogleAPIVersion

    @MockK
    lateinit var submissionQuota: SubmissionQuota

    @MockK
    lateinit var exampleConfiguration: ExposureConfiguration
    private val exampleKeyFiles = listOf(File("file1"), File("file2"))
    private val exampleToken = "123e4567-e89b-12d3-a456-426655440000"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { submissionQuota.consumeQuota(any()) } returns true

        coEvery {
            googleENFClient.provideDiagnosisKeys(
                any(),
                any(),
                any()
            )
        } returns MockGMSTask.forValue(null)

        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createProvider() = DefaultDiagnosisKeyProvider(
        googleAPIVersion = googleAPIVersion,
        submissionQuota = submissionQuota,
        enfClient = googleENFClient
    )

    @Test
    fun `legacy key provision is used on older ENF versions`() {
        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns false

        val provider = createProvider()

        runBlocking {
            provider.provideDiagnosisKeys(exampleKeyFiles, exampleConfiguration, exampleToken)
        }

        coVerify(exactly = 0) {
            googleENFClient.provideDiagnosisKeys(
                exampleKeyFiles, exampleConfiguration, exampleToken
            )
        }

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(
                listOf(exampleKeyFiles[0]), exampleConfiguration, exampleToken
            )
            googleENFClient.provideDiagnosisKeys(
                listOf(exampleKeyFiles[1]), exampleConfiguration, exampleToken
            )
            submissionQuota.consumeQuota(2)
        }
    }

    @Test
    fun `normal key provision is used on newer ENF versions`() {
        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns true

        val provider = createProvider()

        runBlocking {
            provider.provideDiagnosisKeys(exampleKeyFiles, exampleConfiguration, exampleToken)
        }

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(any(), any(), any())
            googleENFClient.provideDiagnosisKeys(
                exampleKeyFiles, exampleConfiguration, exampleToken
            )
            submissionQuota.consumeQuota(1)
        }
    }

    @Test
    fun `passing an a null configuration leads to constructing a fallback from defaults`() {
        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns true

        val provider = createProvider()
        val fallback = ExposureConfiguration.ExposureConfigurationBuilder().build()

        runBlocking {
            provider.provideDiagnosisKeys(exampleKeyFiles, null, exampleToken)
        }

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(any(), any(), any())
            googleENFClient.provideDiagnosisKeys(exampleKeyFiles, fallback, exampleToken)
        }
    }

    @Test
    fun `passing an a null configuration leads to constructing a fallback from defaults, legacy`() {
        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns false

        val provider = createProvider()
        val fallback = ExposureConfiguration.ExposureConfigurationBuilder().build()

        runBlocking {
            provider.provideDiagnosisKeys(exampleKeyFiles, null, exampleToken)
        }

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(
                listOf(exampleKeyFiles[0]), fallback, exampleToken
            )
            googleENFClient.provideDiagnosisKeys(
                listOf(exampleKeyFiles[1]), fallback, exampleToken
            )
            submissionQuota.consumeQuota(2)
        }
    }

    @Test
    fun `quota is consumed silenently`() {
        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns true
        coEvery { submissionQuota.consumeQuota(any()) } returns false

        val provider = createProvider()

        runBlocking {
            provider.provideDiagnosisKeys(exampleKeyFiles, exampleConfiguration, exampleToken)
        }

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(any(), any(), any())
            googleENFClient.provideDiagnosisKeys(
                exampleKeyFiles, exampleConfiguration, exampleToken
            )
            submissionQuota.consumeQuota(1)
        }
    }

    @Test
    fun `quota is consumed silently, legacy`() {
        coEvery { googleAPIVersion.isAtLeast(GoogleAPIVersion.V16) } returns false
        coEvery { submissionQuota.consumeQuota(any()) } returns false

        val provider = createProvider()

        runBlocking {
            provider.provideDiagnosisKeys(exampleKeyFiles, exampleConfiguration, exampleToken)
        }

        coVerify(exactly = 0) {
            googleENFClient.provideDiagnosisKeys(
                exampleKeyFiles, exampleConfiguration, exampleToken
            )
        }

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(
                listOf(exampleKeyFiles[0]), exampleConfiguration, exampleToken
            )
            googleENFClient.provideDiagnosisKeys(
                listOf(exampleKeyFiles[1]), exampleConfiguration, exampleToken
            )
            submissionQuota.consumeQuota(2)
        }
    }
}
