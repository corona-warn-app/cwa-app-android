package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import de.rki.coronawarnapp.nearby.modules.version.OutdatedENFVersionException
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask
import java.io.File

class DefaultDiagnosisKeyProviderTest : BaseTest() {
    @MockK lateinit var googleENFClient: ExposureNotificationClient
    @MockK lateinit var enfVersion: ENFVersion
    @MockK lateinit var submissionQuota: SubmissionQuota

    private val exampleKeyFiles = listOf(File("file1"), File("file2"))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { submissionQuota.consumeQuota(any()) } returns true

        coEvery { googleENFClient.provideDiagnosisKeys(any<List<File>>()) } returns MockGMSTask.forValue(null)

        coEvery { enfVersion.requireMinimumVersion(any()) } returns Unit
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
    fun `provide diagnosis keys with outdated ENF versions`() {
        coEvery { enfVersion.requireMinimumVersion(any()) } throws OutdatedENFVersionException(
            current = 9000,
            required = 5000
        )

        val provider = createProvider()

        assertThrows<OutdatedENFVersionException> {
            runBlockingTest { provider.provideDiagnosisKeys(exampleKeyFiles) } shouldBe false
        }

        coVerify {
            googleENFClient wasNot Called
            submissionQuota wasNot Called
        }
    }

    @Test
    fun `key provision is used on newer ENF versions`() {
        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(exampleKeyFiles) } shouldBe true

        coVerifySequence {
            submissionQuota.consumeQuota(1)
            googleENFClient.provideDiagnosisKeys(exampleKeyFiles)
        }
    }

    @Test
    fun `quota is just monitored`() {
        coEvery { submissionQuota.consumeQuota(any()) } returns false

        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(exampleKeyFiles) } shouldBe true

        coVerifySequence {
            submissionQuota.consumeQuota(1)
            googleENFClient.provideDiagnosisKeys(exampleKeyFiles)
        }
    }

    @Test
    fun `provide empty key list`() {
        val provider = createProvider()

        runBlocking { provider.provideDiagnosisKeys(emptyList()) } shouldBe true

        coVerify {
            googleENFClient wasNot Called
            submissionQuota wasNot Called
        }
    }
}
