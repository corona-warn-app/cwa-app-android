package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import io.kotest.matchers.shouldBe
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
    @MockK lateinit var googleENFClient: ExposureNotificationClient
    @MockK lateinit var enfVersion: ENFVersion
    @MockK lateinit var submissionQuota: SubmissionQuota

    @MockK
    lateinit var submissionQuota: SubmissionQuota

    private val exampleKeyFiles = listOf(File("file1"), File("file2"))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { submissionQuota.consumeQuota(any()) } returns true

        coEvery { googleENFClient.provideDiagnosisKeys(any<List<File>>()) } returns MockGMSTask.forValue(null)

        coEvery { enfVersion.isAtLeast(ENFVersion.V16) } returns true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createProvider() = DefaultDiagnosisKeyProvider(
        enfVersion = enfVersion,
        submissionQuota = submissionQuota,
        enfClient = googleENFClient
    )

    @Test
    fun `key provision is used on older ENF versions`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V15) } returns false

        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(exampleKeyFiles) } shouldBe false

        coVerify(exactly = 0) {
            googleENFClient.provideDiagnosisKeys(exampleKeyFiles)
            googleENFClient.provideDiagnosisKeys(listOf(exampleKeyFiles[0]))
            googleENFClient.provideDiagnosisKeys(listOf(exampleKeyFiles[1]))
            submissionQuota.consumeQuota(2)
        }
    }

    @Test
    fun `key provision is used on newer ENF versions`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V15) } returns true

        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(exampleKeyFiles) } shouldBe true

        coVerify(exactly = 1) {
            googleENFClient.provideDiagnosisKeys(any<List<File>>())
            googleENFClient.provideDiagnosisKeys(exampleKeyFiles)
            submissionQuota.consumeQuota(1)
        }
    }

    @Test
    fun `quota is consumed silently`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V15) } returns true
        coEvery { submissionQuota.consumeQuota(any()) } returns false

        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(exampleKeyFiles) } shouldBe false

        coVerify(exactly = 0) {
            googleENFClient.provideDiagnosisKeys(any<List<File>>())
            googleENFClient.provideDiagnosisKeys(exampleKeyFiles)
        }

        coVerify(exactly = 1) { submissionQuota.consumeQuota(1) }
    }

    @Test
    fun `provide empty key list`() {
        coEvery { enfVersion.isAtLeast(ENFVersion.V15) } returns true

        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(emptyList()) } shouldBe true

        coVerify(exactly = 0) {
            googleENFClient.provideDiagnosisKeys(any<List<File>>())
            googleENFClient.provideDiagnosisKeys(emptyList())
        }
    }
}
