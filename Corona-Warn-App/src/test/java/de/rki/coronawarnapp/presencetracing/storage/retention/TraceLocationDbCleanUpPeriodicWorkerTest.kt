package de.rki.coronawarnapp.presencetracing.storage.retention

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TraceLocationDbCleanUpPeriodicWorkerTest {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var traceLocationCleaner: TraceLocationCleaner
    @MockK lateinit var checkInCleaner: CheckInCleaner

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createWorker() = TraceLocationDbCleanUpPeriodicWorker(
        context = context,
        workerParams = workerParams,
        traceLocationCleaner = traceLocationCleaner,
        checkInCleaner = checkInCleaner
    )

    @Test
    fun `worker should perform cleanUp`() = runTest {
        coEvery { traceLocationCleaner.cleanUp() } just Runs
        coEvery { checkInCleaner.cleanUp() } just Runs

        createWorker().doWork() shouldBe ListenableWorker.Result.Success()

        coVerify { traceLocationCleaner.cleanUp() }
        coVerify { checkInCleaner.cleanUp() }
    }
}
