package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import de.rki.coronawarnapp.util.GoogleAPIVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

@Suppress("DEPRECATION")
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

        val taskResult = mockk<Task<Void>>()
        every { taskResult.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<Nothing>>(0)
            listener.onSuccess(null)
            taskResult
        }
        every { taskResult.addOnFailureListener(any()) } returns taskResult
        coEvery { googleENFClient.provideDiagnosisKeys(any(), any(), any()) } returns taskResult
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
}
