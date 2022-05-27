package de.rki.coronawarnapp.nearby.modules.detectiontracker

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TrackedExposureDetectionTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `isCalculating flag depends on finishedAt`() {
        val initial = TrackedExposureDetection(
            identifier = "123",
            startedAt = Instant.EPOCH
        )
        initial.isCalculating shouldBe true

        val finished = initial.copy(finishedAt = Instant.EPOCH)
        finished.isCalculating shouldBe false
    }
}
