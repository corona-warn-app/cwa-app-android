package de.rki.coronawarnapp.nearby.modules.detectiontracker

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.UUID

class ExposureDetectionTrackerExtensionsTest : BaseTest() {

    @MockK lateinit var tracker: ExposureDetectionTracker

    private val fakeCalculations: MutableStateFlow<Map<String, TrackedExposureDetection>> = MutableStateFlow(emptyMap())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { tracker.calculations } returns fakeCalculations
    }

    @AfterEach
    fun teardown() {
    }

    private fun createFakeCalculation(
        startedAt: Instant,
        result: TrackedExposureDetection.Result? = TrackedExposureDetection.Result.NO_MATCHES
    ) = TrackedExposureDetection(
        identifier = UUID.randomUUID().toString(),
        startedAt = startedAt,
        finishedAt = if (result != null) startedAt.plus(100) else null,
        result = result
    )

    @Test
    fun `last submission`() {
        val tr1 = createFakeCalculation(startedAt = Instant.EPOCH)
        val tr2 = createFakeCalculation(startedAt = Instant.EPOCH.plus(1))
        val tr3 = createFakeCalculation(startedAt = Instant.EPOCH.plus(2), result = null)
        fakeCalculations.value = mapOf(
            tr1.identifier to tr1,
            tr2.identifier to tr2,
            tr3.identifier to tr3
        )
        runBlockingTest {
            tracker.lastSubmission(onlyFinished = false) shouldBe tr3
            tracker.lastSubmission(onlyFinished = true) shouldBe tr2
        }
    }

    @Test
    fun `last submission on empty data`() {
        runBlockingTest {
            tracker.lastSubmission(onlyFinished = false) shouldBe null
            tracker.lastSubmission(onlyFinished = true) shouldBe null
        }
    }

    @Test
    fun `latest submission`() {
        val tr1 = createFakeCalculation(startedAt = Instant.EPOCH)
        val tr2 = createFakeCalculation(startedAt = Instant.EPOCH.plus(1))
        val tr3 = createFakeCalculation(startedAt = Instant.EPOCH.plus(2), result = null)
        fakeCalculations.value = mapOf(
            tr1.identifier to tr1,
            tr2.identifier to tr2,
            tr3.identifier to tr3
        )
        runBlockingTest {
            tracker.latestSubmission(onlySuccessful = false).first() shouldBe tr3
            tracker.latestSubmission(onlySuccessful = true).first() shouldBe tr2
        }
    }

    @Test
    fun `latest submission on empty data`() = runBlockingTest {
        tracker.latestSubmission(onlySuccessful = false).first() shouldBe null
        tracker.latestSubmission(onlySuccessful = true).first() shouldBe null

        val tr1 = createFakeCalculation(startedAt = Instant.EPOCH)
        fakeCalculations.value = mapOf(tr1.identifier to tr1)

        tracker.latestSubmission(onlySuccessful = false).first() shouldBe tr1
        tracker.latestSubmission(onlySuccessful = true).first() shouldBe tr1
    }
}
