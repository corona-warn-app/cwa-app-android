package de.rki.coronawarnapp.covidcertificate.booster

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BoosterCheckWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParameters: WorkerParameters
    @MockK lateinit var boosterNotificationService: BoosterNotificationService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { boosterNotificationService.checkBoosterNotification() } just runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorker() = BoosterCheckWorker(
        context = context,
        workerParams = workerParameters,
        boosterNotificationService = boosterNotificationService
    )

    @Test
    fun `happy street`() = runBlockingTest {
        createWorker().doWork() shouldBe ListenableWorker.Result.success()

        coVerifySequence {
            boosterNotificationService.checkBoosterNotification()
        }
    }

    @Test
    fun `retry on errors`() = runBlockingTest {
        coEvery { boosterNotificationService.checkBoosterNotification() } throws RuntimeException("Test error")

        createWorker().doWork() shouldBe ListenableWorker.Result.retry()

        coVerifySequence {
            boosterNotificationService.checkBoosterNotification()
        }
    }
}
