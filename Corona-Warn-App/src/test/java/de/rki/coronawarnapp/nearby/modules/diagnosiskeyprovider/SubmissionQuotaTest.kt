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
    fun `quota consumption automatically fills up quota if possible`() {
        val quota = createQuota()

        // Reset is at 00:00:00UTC, + safety margin, +1 to be AFTER our margin
        val timeTravelTarget = Instant.parse("2020-08-02T00:01:00.001Z")

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

}
