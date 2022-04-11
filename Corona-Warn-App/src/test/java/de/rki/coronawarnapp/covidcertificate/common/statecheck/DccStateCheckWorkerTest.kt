package de.rki.coronawarnapp.covidcertificate.common.statecheck

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateNotificationService
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccStateCheckWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var dccValidityStateNotificationService: DccValidityStateNotificationService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        dccValidityStateNotificationService.apply {
            coEvery { showNotificationIfStateChanged() } just Runs
        }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorker() = DccStateCheckWorker(
        context = context,
        workerParams = workerParams,
        dccValidityStateNotificationService = dccValidityStateNotificationService,
    )

    @Test
    fun `happy path`() = runBlockingTest {
        createWorker().doWork() shouldBe ListenableWorker.Result.success()

        coVerifySequence {
            dccValidityStateNotificationService.showNotificationIfStateChanged()
        }
    }

    @Test
    fun `retry on errors`() = runBlockingTest {
        coEvery { dccValidityStateNotificationService.showNotificationIfStateChanged() } throws RuntimeException()

        createWorker().doWork() shouldBe ListenableWorker.Result.retry()

        coVerifySequence {
            dccValidityStateNotificationService.showNotificationIfStateChanged()
        }
    }
}
