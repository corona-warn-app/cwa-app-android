package de.rki.coronawarnapp.nearby.modules.calculationtracker

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CalculationTrackerExtensionsTest : BaseTest() {

    @MockK lateinit var calculationTracker: CalculationTracker

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
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
            calculationTracker.isCurrentlyCalculating().first() shouldBe false
            calculationTracker.latestFinishedCalculation().first()!!.token shouldBe "2"
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
            calculationTracker.isCurrentlyCalculating().first() shouldBe true
            calculationTracker.latestFinishedCalculation().first()!!.token shouldBe "1"
        }
    }
}
