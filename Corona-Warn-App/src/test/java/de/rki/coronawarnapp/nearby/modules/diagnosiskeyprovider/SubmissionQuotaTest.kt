package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import de.rki.coronawarnapp.nearby.ENFClientLocalData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
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
            Unit
        }
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00.000Z")
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
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

        runBlocking {
            quota.consumeQuota(5) shouldBe true
        }

        coVerify { enfData.currentQuota = 20 }

        // Reset to 20, then consumed 5
        testStorageCurrentQuota shouldBe 15
    }

    @Test
    fun `quota consumption return true if quota was available`() {
        testStorageCurrentQuota shouldBe 20

        val quota = createQuota()

        runBlocking {
            quota.consumeQuota(10) shouldBe true
            quota.consumeQuota(10) shouldBe true
            quota.consumeQuota(10) shouldBe false
            quota.consumeQuota(1) shouldBe false
        }

        verify(exactly = 4) { timeStamper.nowUTC }
    }

    @Test
    fun `consumption of 0 quota is handled`() {
        val quota = createQuota()

        runBlocking {
            quota.consumeQuota(0) shouldBe true
            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(0) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }
    }

    @Test
    fun `partial consumption is not possible`() {
        testStorageCurrentQuota shouldBe 20

        val quota = createQuota()

        runBlocking {
            quota.consumeQuota(18) shouldBe true
            quota.consumeQuota(1) shouldBe true
            quota.consumeQuota(2) shouldBe false
        }
    }

    @Test
    fun `quota consumption automatically fills up quota if possible`() {
        val quota = createQuota()

        // Reset is at 00:00:00UTC, we trigger at 1 milisecond after midnight
        val timeTravelTarget = Instant.parse("2020-12-24T00:00:00.001Z")

        runBlocking {
            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(20) shouldBe false

            every { timeStamper.nowUTC } returns timeTravelTarget

            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }

        coVerify(exactly = 1) { enfData.currentQuota = 20 }
        verify(exactly = 4) { timeStamper.nowUTC }
        verify(exactly = 1) { enfData.lastQuotaResetAt = timeTravelTarget }
    }

    @Test
    fun `quota fill up is at midnight`() {
        testStorageCurrentQuota = 20
        testStorageLastQuotaReset = Instant.parse("2020-12-24T23:00:00.000Z")
        val startTime = Instant.parse("2020-12-24T23:59:59.998Z")
        every { timeStamper.nowUTC } returns startTime

        val quota = createQuota()

        runBlocking {
            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(1) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plus(1)
            quota.consumeQuota(1) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plus(2)
            quota.consumeQuota(1) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plus(3)
            quota.consumeQuota(1) shouldBe true

            every { timeStamper.nowUTC } returns startTime.plus(4)
            quota.consumeQuota(20) shouldBe false

            every { timeStamper.nowUTC } returns startTime.plus(3).plus(Duration.standardDays(1))
            quota.consumeQuota(20) shouldBe true
        }
    }

    @Test
    fun `large time gaps are no issue`() {
        val startTime = Instant.parse("2020-12-24T20:00:00.000Z")

        runBlocking {
            every { timeStamper.nowUTC } returns startTime
            val quota = createQuota()
            quota.consumeQuota(17) shouldBe true
        }

        runBlocking {
            every { timeStamper.nowUTC } returns startTime.plus(Duration.standardDays(365))
            val quota = createQuota()
            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }

        runBlocking {
            every { timeStamper.nowUTC } returns startTime.plus(Duration.standardDays(365 * 2))
            val quota = createQuota()
            quota.consumeQuota(17) shouldBe true
        }
        runBlocking {
            every { timeStamper.nowUTC } returns startTime.plus(Duration.standardDays(365 * 3))
            val quota = createQuota()
            quota.consumeQuota(3) shouldBe true
            quota.consumeQuota(17) shouldBe true
            quota.consumeQuota(1) shouldBe false
        }
    }

    @Test
    fun `reverse timetravel is handled `() {
        testStorageLastQuotaReset = Instant.parse("2020-12-24T23:00:00.000Z")
        val startTime = Instant.parse("2020-12-24T23:59:59.999Z")
        every { timeStamper.nowUTC } returns startTime

        val quota = createQuota()

        runBlocking {
            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(1) shouldBe false

            // Go forward and get a reset
            every { timeStamper.nowUTC } returns startTime.plus(Duration.standardHours(1))
            quota.consumeQuota(20) shouldBe true
            quota.consumeQuota(1) shouldBe false

            // Go backwards and don't gain a reset
            every { timeStamper.nowUTC } returns startTime.minus(Duration.standardHours(1))
            quota.consumeQuota(1) shouldBe false

            // Go forward again, but no new reset happens
            every { timeStamper.nowUTC } returns startTime.plus(Duration.standardHours(1))
            quota.consumeQuota(1) shouldBe false
        }
    }
}
