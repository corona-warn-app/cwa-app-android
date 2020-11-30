package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelTaskConfigTest : BaseTest() {

    @MockK lateinit var exposureDetectionTracker: ExposureDetectionTracker

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `risk level task max execution time is not above 9 minutes`() {
        RiskLevelTask.Config(exposureDetectionTracker)
            .executionTimeout
            .isShorterThan(Duration.standardMinutes(9)) shouldBe true
    }

    @Test
    fun `risk level preconditions are met`() {
        every { exposureDetectionTracker.calculations } returns MutableStateFlow(mapOf("" to TrackedExposureDetection(
            identifier = "",
            startedAt = Instant(),
            result = TrackedExposureDetection.Result.NO_MATCHES,
            enfVersion = TrackedExposureDetection.EnfVersion.V2_WINDOW_MODE
        )))
        runBlocking {
            RiskLevelTask.Config(exposureDetectionTracker)
                .preconditions.fold(true) { result, precondition ->
                    result && precondition()
                } shouldBe true
        }
    }

    @Test
    fun `risk level preconditions are not met, because there are no detections`() {
        every { exposureDetectionTracker.calculations } returns MutableStateFlow(emptyMap())
            runBlocking {
            RiskLevelTask.Config(exposureDetectionTracker)
                .preconditions.fold(true) { result, precondition ->
                    result && precondition()
                } shouldBe false
        }
    }

    @Test
    fun `risk level preconditions are not met, because there are no enf V2 detections`() {
        every { exposureDetectionTracker.calculations } returns MutableStateFlow(mapOf("" to TrackedExposureDetection(
            identifier = "",
            startedAt = Instant(),
            result = TrackedExposureDetection.Result.NO_MATCHES,
            enfVersion = TrackedExposureDetection.EnfVersion.V1_LEGACY_MODE
        )))
        runBlocking {
            RiskLevelTask.Config(exposureDetectionTracker)
                .preconditions.fold(true) { result, precondition ->
                    result && precondition()
                } shouldBe false
        }
    }

    @Test
    fun `risk level preconditions are not met, because detection is not finished yet`() {
        every { exposureDetectionTracker.calculations } returns MutableStateFlow(mapOf("" to TrackedExposureDetection(
            identifier = "",
            startedAt = Instant(),
            enfVersion = TrackedExposureDetection.EnfVersion.V2_WINDOW_MODE
        )))
        runBlocking {
            RiskLevelTask.Config(exposureDetectionTracker)
                .preconditions.fold(true) { result, precondition ->
                    result && precondition()
                } shouldBe false
        }
    }
}
