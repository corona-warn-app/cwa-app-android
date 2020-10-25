package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.calculationtracker.Calculation
import de.rki.coronawarnapp.nearby.modules.calculationtracker.CalculationTracker
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.locationless.ScanningSupport
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifySequence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

@Suppress("DEPRECATION")
class ENFClientTest : BaseTest() {

    @MockK lateinit var googleENFClient: ExposureNotificationClient

    @MockK lateinit var diagnosisKeyProvider: DiagnosisKeyProvider
    @MockK lateinit var tracingStatus: TracingStatus
    @MockK lateinit var scanningSupport: ScanningSupport
    @MockK lateinit var calculationTracker: CalculationTracker

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any()) } returns true
        every { calculationTracker.trackNewCalaculation(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createClient() = ENFClient(
        googleENFClient = googleENFClient,
        diagnosisKeyProvider = diagnosisKeyProvider,
        tracingStatus = tracingStatus,
        scanningSupport = scanningSupport,
        calculationTracker = calculationTracker
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

    @Test
    fun `tracing status check is forwaded to the right module`() = runBlocking {
        every { tracingStatus.isTracingEnabled } returns flowOf(true)

        val client = createClient()
        client.isTracingEnabled.toList().single() shouldBe true

        verifySequence {
            tracingStatus.isTracingEnabled
        }
    }

    @Test
    fun `locationless scanning support check is forwaded to the right module`() = runBlocking {
        every { scanningSupport.isLocationLessScanningSupported } returns flowOf(true)

        val client = createClient()
        client.isLocationLessScanningSupported.toList().single() shouldBe true

        verifySequence {
            scanningSupport.isLocationLessScanningSupported
        }
    }

    @Test
    fun `validate extensions`() {
        runBlocking {
            val calculations = flowOf(
                mapOf(
                    "1" to Calculation(
                        token = "1",
                        state = Calculation.State.DONE,
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH
                    ),
                    "2" to Calculation(
                        token = "2",
                        state = Calculation.State.DONE,
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH.plus(1)
                    )
                )
            )

            every { calculationTracker.calculations } returns calculations
            createClient().apply {
                isCurrentlyCalculating().first() shouldBe false
                latestFinishedCalculation().first()!!.token shouldBe "2"
            }

        }

        runBlocking {
            val calculations = flowOf(
                mapOf(
                    "1" to Calculation(
                        token = "1",
                        state = Calculation.State.DONE,
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH.plus(2)
                    ),
                    "2" to Calculation(
                        token = "2",
                        state = Calculation.State.CALCULATING,
                        startedAt = Instant.EPOCH,
                    ),
                    "3" to Calculation(
                        token = "3",
                        state = Calculation.State.DONE,
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH
                    )
                )
            )

            every { calculationTracker.calculations } returns calculations

            createClient().apply {
                isCurrentlyCalculating().first() shouldBe true
                latestFinishedCalculation().first()!!.token shouldBe "1"
            }
        }
    }
}
