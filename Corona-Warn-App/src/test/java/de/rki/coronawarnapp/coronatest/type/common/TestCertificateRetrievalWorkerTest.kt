package de.rki.coronawarnapp.coronatest.type.common

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.execution.TestCertificateRetrievalWorker
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.net.UnknownHostException

class TestCertificateRetrievalWorkerTest : BaseTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var request: WorkRequest
    @MockK lateinit var testCertificateRepository: TestCertificateRepository

    @RelaxedMockK lateinit var workerParams: WorkerParameters

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { testCertificateRepository.refresh() } returns emptySet()
    }

    private fun createWorker(
        runAttempts: Int = 0
    ) = TestCertificateRetrievalWorker(
        context = context,
        workerParams = workerParams.also {
            every { it.runAttemptCount } returns runAttempts
        },
        testCertificateRepository = testCertificateRepository,
    )

    @Test
    fun `certificate refresh`() = runBlockingTest {
        val result = createWorker().doWork()

        coVerify(exactly = 1) { testCertificateRepository.refresh() }

        result shouldBe ListenableWorker.Result.success()
    }

    @Test
    fun `retry on error`() = runBlockingTest {
        coEvery { testCertificateRepository.refresh() } throws UnknownHostException()

        val result = createWorker().doWork()

        coVerify(exactly = 1) { testCertificateRepository.refresh() }

        result shouldBe ListenableWorker.Result.retry()
    }

    @Test
    fun `failure after 2 retries`() = runBlockingTest {

        val result = createWorker(runAttempts = 3).doWork()

        coVerify(exactly = 0) { testCertificateRepository.refresh() }

        result shouldBe ListenableWorker.Result.failure()
    }
}
