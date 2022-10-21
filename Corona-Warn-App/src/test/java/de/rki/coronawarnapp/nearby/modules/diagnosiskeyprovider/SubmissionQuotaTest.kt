package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import de.rki.coronawarnapp.nearby.ENFClientLocalData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore

class SubmissionQuotaTest : BaseTest() {
    lateinit var enfData: ENFClientLocalData

    @MockK
    lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        enfData = ENFClientLocalData(FakeDataStore())
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00.000Z")
    }

    private fun createQuota() = SubmissionQuota(
        enfData = enfData,
        timeStamper = timeStamper
    )

    @Test
    fun `first init sets a sane default quota`() = runTest2 {
        val quota = createQuota()

        quota.consumeQuota(5) shouldBe true
        enfData.currentQuota.first() shouldBe 1
    }

    @Test
    fun `quota consumption return true if quota was available`() = runTest2 {
        enfData.updateCurrentQuota(6)

        val quota = createQuota()

        quota.consumeQuota(3) shouldBe true
        quota.consumeQuota(3) shouldBe true
        quota.consumeQuota(1) shouldBe false

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
    fun `partial consumption is not possible`() = runTest2 {
        enfData.updateCurrentQuota(6)

        val quota = createQuota()

        quota.consumeQuota(4) shouldBe true
        quota.consumeQuota(1) shouldBe true
        quota.consumeQuota(2) shouldBe false
    }

    @Test
    fun `quota consumption automatically fills up quota if possible`() = runTest2 {
        val quota = createQuota()

        // Reset is at 00:00:00UTC, we trigger at 1 milisecond after midnight
        val timeTravelTarget = Instant.parse("2020-12-24T00:00:00.001Z")

        quota.consumeQuota(6) shouldBe true
        quota.consumeQuota(6) shouldBe false

        every { timeStamper.nowUTC } returns timeTravelTarget

        quota.consumeQuota(6) shouldBe true
        quota.consumeQuota(1) shouldBe false

        // TODO
        // coVerify(exactly = 1) { enfData.currentQuota = 6 }
        verify(exactly = 4) { timeStamper.nowUTC }

        enfData.lastQuotaResetAt.first() shouldBe timeTravelTarget
    }

    @Test
    fun `quota fill up is at midnight`() = runTest2 {
        enfData.updateCurrentQuota(6)
        val startTime = Instant.parse("2020-12-24T23:59:59.998Z")
        every { timeStamper.nowUTC } returns startTime

        val quota = createQuota()

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

    @Test
    fun `large time gaps are no issue`() = runTest2 {
        val startTime = Instant.parse("2020-12-24T20:00:00.000Z")

        var quota = createQuota()

        every { timeStamper.nowUTC } returns startTime
        quota.consumeQuota(3) shouldBe true

        every { timeStamper.nowUTC } returns startTime.plus(Duration.ofDays(365))
        quota = createQuota()
        quota.consumeQuota(6) shouldBe true
        quota.consumeQuota(1) shouldBe false

        every { timeStamper.nowUTC } returns startTime.plus(Duration.ofDays(365 * 2))
        quota = createQuota()
        quota.consumeQuota(3) shouldBe true

        every { timeStamper.nowUTC } returns startTime.plus(Duration.ofDays(365 * 3))
        quota = createQuota()
        quota.consumeQuota(3) shouldBe true
        quota.consumeQuota(3) shouldBe true
        quota.consumeQuota(1) shouldBe false
    }

    @Test
    fun `reverse timetravel is handled `() = runTest2 {
        val startTime = Instant.parse("2020-12-24T23:59:59.999Z")
        every { timeStamper.nowUTC } returns startTime

        val quota = createQuota()

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
