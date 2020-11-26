package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.joda.time.Duration
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
        val config = RiskLevelTask.Config(exposureDetectionTracker)
        config.executionTimeout.isShorterThan(Duration.standardMinutes(9)) shouldBe true
    }
}
