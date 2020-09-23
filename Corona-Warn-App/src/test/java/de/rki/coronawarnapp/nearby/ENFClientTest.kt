package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

@Suppress("DEPRECATION")
class ENFClientTest : BaseTest() {

    @MockK
    lateinit var googleENFClient: ExposureNotificationClient

    @MockK
    lateinit var diagnosisKeyProvider: DiagnosisKeyProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any()) } returns true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createClient() = ENFClient(
        googleENFClient = googleENFClient,
        diagnosisKeyProvider = diagnosisKeyProvider
    )

    @Test
    fun `internal enf client is available as workaround`() {
        val client = createClient()
        client.internalClient shouldBe googleENFClient
    }

    @Test
    fun `provide diagnosis key call is forwarded to the right module`() {
        val client = createClient()
        val keyFiles = listOf(File("test"))
        val configuration = mockk<ExposureConfiguration>()
        val token = "123"

        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any()) } returns true
        runBlocking {
            client.provideDiagnosisKeys(keyFiles, configuration, token) shouldBe true
        }

        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any()) } returns false
        runBlocking {
            client.provideDiagnosisKeys(keyFiles, configuration, token) shouldBe false
        }

        coVerify(exactly = 2) {
            diagnosisKeyProvider.provideDiagnosisKeys(
                keyFiles,
                configuration,
                token
            )
        }
    }
}
