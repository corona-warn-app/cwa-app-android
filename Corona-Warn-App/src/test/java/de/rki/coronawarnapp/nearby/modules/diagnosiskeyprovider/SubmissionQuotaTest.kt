package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import de.rki.coronawarnapp.nearby.ENFClientLocalData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SubmissionQuotaTest : BaseTest() {
    @MockK
    lateinit var enfData: ENFClientLocalData

    @MockK
    lateinit var timeStamper: TimeStamper

    private var testStorageCurrentQuota = SubmissionQuota.DEFAULT_QUOTA
    private var testStorageLastQuotaReset = Instant.parse("2020-08-01T01:00:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { enfData.currentQuota = any() } answers {
            testStorageCurrentQuota = arg(0)
            Unit
        }
        every { enfData.currentQuota } answers {
            testStorageCurrentQuota
        }
        every { enfData.lastQuotaResetAt } answers {
            testStorageLastQuotaReset
        }
        every { enfData.lastQuotaResetAt = any() } answers {
            testStorageLastQuotaReset = arg(0)
        }
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00.000Z")
    }

    private fun createQuota() = SubmissionQuota(
        enfData = enfData,
        timeStamper = timeStamper
    )

    @Test
    fun `first init sets a sane default quota`() {
        // The default lastQuotaReset is at 0L EPOCH Millis
        testStorageLastQuotaReset = Instant.EPOCH

        val quota = createQuota()

        runTest {
            quota.consumeQuota(5) shouldBe true
        }

        coVerify { enfData.currentQuota = 6 }

        // Reset to 20, then consumed 5
        testStorageCurrentQuota shouldBe 1
    }

    @Test
    fun `quota consumption return true if quota was available`() {
        testStorageCurrentQuota shouldBe 6

        val quota = createQuota()

        runTest {
            quota.consumeQuota(3) shouldBe true
            quota.consumeQuota(3) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }

        verify(exactly = 3) { timeStamper.nowUTC }
    }

    @Test
    fun `consumption of 0 quota is handled`() {
        val quota = createQuota()

        runTest {
            quota.consumeQuota(0) shouldBe true
            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(0) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }
    }

    @Test
    fun `partial consumption is not possible`() {
        testStorageCurrentQuota shouldBe 6

        val quota = createQuota()

        runTest {
            quota.consumeQuota(4) shouldBe true
            quota.consumeQuota(1) shouldBe true
            quota.consumeQuota(2) shouldBe false
        }
    }

    @Test
    fun `quota consumption automatically fills up quota if possible`() {
        val quota = createQuota()

        // Reset is at 00:00:00UTC, we trigger at 1 milisecond after midnight
        val timeTravelTarget = Instant.parse("2020-12-24T00:00:00.001Z")

        runTest {
            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(6) shouldBe false

            every { timeStamper.nowUTC } returns timeTravelTarget

            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }

        coVerify(exactly = 1) { enfData.currentQuota = 6 }
        verify(exactly = 4) { timeStamper.nowUTC }
        verify(exactly = 1) { enfData.lastQuotaResetAt = timeTravelTarget }
    }

    @Test
    fun `quota fill up is at midnight`() {
        testStorageCurrentQuota = 6
        testStorageLastQuotaReset = Instant.parse("2020-12-24T23:00:00.000Z")
        val startTime = Instant.parse("2020-12-24T23:59:59.998Z")
        every { timeStamper.nowUTC } returns startTime

        val quota = createQuota()

        runTest {
            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(1) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plusMillis(1)
            quota.consumeQuota(1) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plusMillis(2)
            quota.consumeQuota(1) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plusMillis(3)
            quota.consumeQuota(1) shouldBe true

            every { timeStamper.nowUTC } returns startTime.plusMillis(4)
            quota.consumeQuota(6) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plusMillis(3).plus(Duration.ofDays(1))
            quota.consumeQuota(6) shouldBe true
        }
    }

    @Test
    fun `large time gaps are no issue`() {
        val startTime = Instant.parse("2020-12-24T20:00:00.000Z")

        runTest {
            every { timeStamper.nowUTC } returns startTime
            val quota = createQuota()
            quota.consumeQuota(3) shouldBe true
        }

        runTest {
            every { timeStamper.nowUTC } returns startTime.plus(Duration.ofDays(365))
            val quota = createQuota()
            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }

        runTest {
            every { timeStamper.nowUTC } returns startTime.plus(Duration.ofDays(365 * 2))
            val quota = createQuota()
            quota.consumeQuota(3) shouldBe true
        }
        runTest {
            every { timeStamper.nowUTC } returns startTime.plus(Duration.ofDays(365 * 3))
            val quota = createQuota()
            quota.consumeQuota(3) shouldBe true
            quota.consumeQuota(3) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }
    }

    @Test
    fun `reverse timetravel is handled `() {
        testStorageLastQuotaReset = Instant.parse("2020-12-24T23:00:00.000Z")
        val startTime = Instant.parse("2020-12-24T23:59:59.999Z")
        every { timeStamper.nowUTC } returns startTime

        val quota = createQuota()

        runTest {
            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(1) shouldBe false

            // Go forward and get a reset
            every { timeStamper.nowUTC } returns startTime.plus(Duration.ofHours(1))
            quota.consumeQuota(6) shouldBe true
            quota.consumeQuota(1) shouldBe false

            // Go backwards and don't gain a reset
            every { timeStamper.nowUTC } returns startTime.minus(Duration.ofHours(1))
            quota.consumeQuota(1) shouldBe false

            // Go forward again, but no new reset happens
            every { timeStamper.nowUTC } returns startTime.plus(Duration.ofHours(1))
            quota.consumeQuota(1) shouldBe false
        }
    }
}
