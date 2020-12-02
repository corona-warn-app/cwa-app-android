package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.exposurewindow.ExposureWindowProvider
import de.rki.coronawarnapp.nearby.modules.locationless.ScanningSupport
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
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

class ENFClientTest : BaseTest() {

    @MockK lateinit var googleENFClient: ExposureNotificationClient

    @MockK lateinit var diagnosisKeyProvider: DiagnosisKeyProvider
    @MockK lateinit var tracingStatus: TracingStatus
    @MockK lateinit var scanningSupport: ScanningSupport
    @MockK lateinit var exposureWindowProvider: ExposureWindowProvider
    @MockK lateinit var exposureDetectionTracker: ExposureDetectionTracker
    @MockK lateinit var enfVersion: ENFVersion
    @MockK lateinit var tekHistoryProvider: TEKHistoryProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any()) } returns true
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
        enfVersion = enfVersion,
        exposureWindowProvider = exposureWindowProvider,
        exposureDetectionTracker = exposureDetectionTracker,
        tekHistoryProvider = tekHistoryProvider
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

        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any()) } returns true
        runBlocking {
            client.provideDiagnosisKeys(keyFiles, mockk()) shouldBe true
        }

        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any()) } returns false
        runBlocking {
            client.provideDiagnosisKeys(keyFiles, mockk()) shouldBe false
        }

        coVerify(exactly = 2) {
            diagnosisKeyProvider.provideDiagnosisKeys(
                keyFiles,
                any()
            )
        }
    }

    @Test
    fun `provide diagnosis key call is only forwarded if there are actually key files`() {
        val client = createClient()
        val keyFiles = emptyList<File>()

        coEvery { diagnosisKeyProvider.provideDiagnosisKeys(any(), any()) } returns true
        runBlocking {
            client.provideDiagnosisKeys(keyFiles, mockk()) shouldBe true
        }

        coVerify(exactly = 0) {
            diagnosisKeyProvider.provideDiagnosisKeys(any(), any())
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

    @Test
    fun `exposure windows check is forwarded to the right module`() = runBlocking {
        val exposureWindowList = emptyList<ExposureWindow>()
        coEvery { exposureWindowProvider.exposureWindows() } returns exposureWindowList

        val client = createClient()
        client.exposureWindows() shouldBe exposureWindowList

        coVerify(exactly = 1) {
            exposureWindowProvider.exposureWindows()
        }
    }

    @Test
    fun `enf version check is forwaded to the right module`() = runBlocking {
        coEvery { enfVersion.getENFClientVersion() } returns Long.MAX_VALUE

        createClient().getENFClientVersion() shouldBe Long.MAX_VALUE

        coVerifySequence { enfVersion.getENFClientVersion() }
    }

    @Test
    fun `tek history provider calls are forwarded to the right module`() = runBlocking {
        val mockTEK = mockk<TemporaryExposureKey>()
        coEvery { tekHistoryProvider.getTEKHistory() } returns listOf(mockTEK)

        createClient().getTEKHistory() shouldBe listOf(mockTEK)

        coVerifySequence { tekHistoryProvider.getTEKHistory() }
    }
}
