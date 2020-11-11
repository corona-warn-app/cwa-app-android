package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
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
    @MockK lateinit var exposureDetectionTracker: ExposureDetectionTracker

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any()) } returns true
        every { exposureDetectionTracker.trackNewExposureDetection(any()) } just Runs
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
        exposureDetectionTracker = exposureDetectionTracker
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
    fun `provide diagnosis key call is only forwarded if there are actually key files`() {
        val client = createClient()
        val keyFiles = emptyList<File>()
        val configuration = mockk<ExposureConfiguration>()
        val token = "123"

        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any()) } returns true
        runBlocking {
            client.provideDiagnosisKeys(keyFiles, configuration, token) shouldBe true
        }

        coVerify(exactly = 0) {
            diagnosisKeyProvider.provideDiagnosisKeys(any(), any(), any())
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
    fun `calculation state depends on the last started calculation`() {
        runBlocking {
            every { exposureDetectionTracker.calculations } returns flowOf(
                mapOf(
                    "1" to TrackedExposureDetection(
                        identifier = "1",
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH
                    ),
                    "2" to TrackedExposureDetection(
                        identifier = "2",
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH.plus(1)
                    )
                )
            )

            createClient().isPerformingExposureDetection().first() shouldBe false
        }

        runBlocking {
            every { exposureDetectionTracker.calculations } returns flowOf(
                mapOf(
                    "1" to TrackedExposureDetection(
                        identifier = "1",
                        startedAt = Instant.EPOCH.plus(5)
                    ),
                    "2" to TrackedExposureDetection(
                        identifier = "2",
                        startedAt = Instant.EPOCH.plus(4),
                        finishedAt = Instant.EPOCH.plus(1)
                    )
                )
            )

            createClient().isPerformingExposureDetection().first() shouldBe true
        }

        runBlocking {
            every { exposureDetectionTracker.calculations } returns flowOf(
                mapOf(
                    "1" to TrackedExposureDetection(
                        identifier = "1",
                        startedAt = Instant.EPOCH
                    ),
                    "2" to TrackedExposureDetection(
                        identifier = "2",
                        startedAt = Instant.EPOCH,
                        finishedAt = Instant.EPOCH.plus(2)
                    ),
                    "3" to TrackedExposureDetection(
                        identifier = "3",
                        startedAt = Instant.EPOCH.plus(1)
                    )
                )
            )

            createClient().isPerformingExposureDetection().first() shouldBe true
        }
    }

    @Test
    fun `validate that we only get the last finished calcluation`() {
        runBlocking {
            every { exposureDetectionTracker.calculations } returns flowOf(
                mapOf(
                    "1" to TrackedExposureDetection(
                        identifier = "1",
                        startedAt = Instant.EPOCH,
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        finishedAt = Instant.EPOCH
                    ),
                    "2" to TrackedExposureDetection(
                        identifier = "2",
                        startedAt = Instant.EPOCH,
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        finishedAt = Instant.EPOCH.plus(1)
                    ),
                    "2-timeout" to TrackedExposureDetection(
                        identifier = "2-timeout",
                        startedAt = Instant.EPOCH,
                        result = TrackedExposureDetection.Result.TIMEOUT,
                        finishedAt = Instant.EPOCH.plus(2)
                    ),
                    "3" to TrackedExposureDetection(
                        identifier = "3",
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        startedAt = Instant.EPOCH.plus(2)
                    )
                )
            )

            createClient().lastSuccessfulTrackedExposureDetection().first()!!.identifier shouldBe "2"
        }

        runBlocking {
            every { exposureDetectionTracker.calculations } returns flowOf(
                mapOf(
                    "0" to TrackedExposureDetection(
                        identifier = "1",
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        startedAt = Instant.EPOCH.plus(3)
                    ),
                    "1-timeout" to TrackedExposureDetection(
                        identifier = "1-timeout",
                        startedAt = Instant.EPOCH,
                        result = TrackedExposureDetection.Result.TIMEOUT,
                        finishedAt = Instant.EPOCH.plus(3)
                    ),
                    "1" to TrackedExposureDetection(
                        identifier = "1",
                        startedAt = Instant.EPOCH,
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        finishedAt = Instant.EPOCH.plus(2)
                    ),
                    "2" to TrackedExposureDetection(
                        identifier = "2",
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        startedAt = Instant.EPOCH
                    ),
                    "3" to TrackedExposureDetection(
                        identifier = "3",
                        startedAt = Instant.EPOCH,
                        result = TrackedExposureDetection.Result.UPDATED_STATE,
                        finishedAt = Instant.EPOCH
                    )
                )
            )

            createClient().lastSuccessfulTrackedExposureDetection().first()!!.identifier shouldBe "1"
        }
    }
}
