package de.rki.coronawarnapp.datadonation.analytics.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.worker.BackgroundConstants
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DataDonationAnalyticsPeriodicWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var analytics: Analytics
    @RelaxedMockK lateinit var workerParams: WorkerParameters

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorker() = DataDonationAnalyticsPeriodicWorker(
        context = context,
        workerParams = workerParams,
        analytics = analytics
    )

    @Test
    fun `if result says retry, do retry`() = runTest {
        coEvery { analytics.submitIfWanted() } returns Analytics.Result(successful = false, shouldRetry = true)
        createWorker().doWork() shouldBe ListenableWorker.Result.Retry()

        coEvery { analytics.submitIfWanted() } returns Analytics.Result(successful = false, shouldRetry = false)
        createWorker().doWork() shouldBe ListenableWorker.Result.Failure()

        coEvery { analytics.submitIfWanted() } returns Analytics.Result(successful = true, shouldRetry = false)
        createWorker().doWork() shouldBe ListenableWorker.Result.Success()
    }

    @Test
    fun `maximum of 2 retry attemtps`() = runTest {
        val worker = createWorker()
        worker.runAttemptCount shouldBe 0

        every { worker.runAttemptCount } returns BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 1

        worker.doWork() shouldBe ListenableWorker.Result.Failure()
    }

    @Test
    fun `unexpected errors do not cause a retry`() = runTest {
        coEvery { analytics.submitIfWanted() } throws Exception("SURPRISE!!!")
        createWorker().doWork() shouldBe ListenableWorker.Result.Failure()
    }
}
