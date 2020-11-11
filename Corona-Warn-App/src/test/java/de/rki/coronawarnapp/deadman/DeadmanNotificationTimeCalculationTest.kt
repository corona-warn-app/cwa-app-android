package de.rki.coronawarnapp.deadman

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationTimeCalculationTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var mockExposureDetection: TrackedExposureDetection

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00.000Z")
        every { enfClient.lastSuccessfulTrackedExposureDetection() } returns flowOf(mockExposureDetection)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createTimeCalculator() = DeadmanNotificationTimeCalculation(
        timeStamper = timeStamper,
        enfClient = enfClient
    )

    @Test
    fun `12 hours difference`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")

        createTimeCalculator().getHoursDiff(Instant.parse("2020-08-27T14:00:00.000Z")) shouldBe 720
    }

    @Test
    fun `negative time difference`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00.000Z")

        createTimeCalculator().getHoursDiff(Instant.parse("2020-08-27T14:00:00.000Z")) shouldBe -2160
    }

    @Test
    fun `success in future case`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")

        createTimeCalculator().getHoursDiff(Instant.parse("2020-08-27T15:00:00.000Z")) shouldBe 2220
    }

    @Test
    fun `12 hours delay`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")
        every { mockExposureDetection.finishedAt } returns Instant.parse("2020-08-27T14:00:00.000Z")

        createTimeCalculator().getDelay() shouldBe 720

        verify(exactly = 1) { enfClient.lastSuccessfulTrackedExposureDetection() }
    }

    @Test
    fun `negative delay`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00.000Z")
        every { mockExposureDetection.finishedAt } returns Instant.parse("2020-08-27T14:00:00.000Z")

        createTimeCalculator().getDelay() shouldBe -2160

        verify(exactly = 1) { enfClient.lastSuccessfulTrackedExposureDetection() }
    }

    @Test
    fun `success in future delay`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")
        every { mockExposureDetection.finishedAt } returns Instant.parse("2020-08-27T15:00:00.000Z")

        createTimeCalculator().getDelay() shouldBe 2220

        verify(exactly = 1) { enfClient.lastSuccessfulTrackedExposureDetection() }
    }

    @Test
    fun `initial delay - no successful calculations yet`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")
        every { enfClient.lastSuccessfulTrackedExposureDetection() } returns flowOf(null)

        createTimeCalculator().getDelay() shouldBe 2160

        verify(exactly = 1) { enfClient.lastSuccessfulTrackedExposureDetection() }
    }
}
