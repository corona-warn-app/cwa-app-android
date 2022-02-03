package de.rki.coronawarnapp.covidcertificate.test.core.execution

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.net.UnknownHostException

class TestCertificateRetrievalWorkerTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var request: WorkRequest
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger

    @RelaxedMockK lateinit var workerParams: WorkerParameters

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { testCertificateRepository.refresh() } returns emptySet()
        coEvery { dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdate() } just Runs
    }

    private fun createWorker(
        runAttempts: Int = 0
    ) = TestCertificateRetrievalWorker(
        context = context,
        workerParams = workerParams.also {
            every { it.runAttemptCount } returns runAttempts
        },
        testCertificateRepository = testCertificateRepository,
        dccWalletInfoUpdateTrigger = dccWalletInfoUpdateTrigger,
    )

    @Test
    fun `certificate refresh`() = runBlockingTest {
        val result = createWorker().doWork()

        coVerify(exactly = 1) { testCertificateRepository.refresh() }

        result shouldBe ListenableWorker.Result.success()

        verify { dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdate() }
    }

    @Test
    fun `retry on error`() = runBlockingTest {
        coEvery { testCertificateRepository.refresh() } throws UnknownHostException()

        val result = createWorker().doWork()

        coVerify(exactly = 1) { testCertificateRepository.refresh() }

        verify(exactly = 0) {  dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdate()  }

        result shouldBe ListenableWorker.Result.retry()
    }

    @Test
    fun `failure after 2 retries`() = runBlockingTest {

        val result = createWorker(runAttempts = 3).doWork()

        coVerify(exactly = 0) { testCertificateRepository.refresh() }

        result shouldBe ListenableWorker.Result.failure()
    }
}
