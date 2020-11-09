package de.rki.coronawarnapp.deadman

import androidx.work.BackoffPolicy
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationWorkBuilderTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `onetime work test`() {
        testOneTimeWork(10L)
        testOneTimeWork(-10L)
        testOneTimeWork(0)
    }

    /**
     * Delay time in minutes
     * Backoff delay 8 minutes
     */
    private fun testOneTimeWork(delay: Long) {
        val periodicWork = DeadmanNotificationWorkBuilder().buildOneTimeWork(delay)

        periodicWork.workSpec.backoffPolicy shouldBe BackoffPolicy.EXPONENTIAL
        periodicWork.workSpec.backoffDelayDuration shouldBe 8 * 60 * 1000
        periodicWork.workSpec.initialDelay shouldBe delay * 60 * 1000
    }

    /**
     * Delay time in minutes
     * Backoff delay 8 minutes
     * Interval duration 1 hour
     */
    @Test
    fun `periodic work test`() {
        val periodicWork = DeadmanNotificationWorkBuilder().buildPeriodicWork()

        periodicWork.workSpec.backoffPolicy shouldBe BackoffPolicy.EXPONENTIAL
        periodicWork.workSpec.backoffDelayDuration shouldBe 8 * 60 * 1000
        periodicWork.workSpec.intervalDuration shouldBe 60 * 60 * 1000
    }
}
