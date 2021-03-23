package de.rki.coronawarnapp.eventregistration.storage.retention

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
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EventRegistrationDbCleanUpPeriodicWorkerTest {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var traceLocationCleaner: TraceLocationCleaner

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createWorker() = EventRegistrationDbCleanUpPeriodicWorker(
        context = context,
        workerParams = workerParams,
        traceLocationCleaner = traceLocationCleaner
    )

    @Test
    fun `worker should perform cleanUp`() = runBlockingTest {
        coEvery { traceLocationCleaner.cleanUp() } just Runs

        createWorker().doWork() shouldBe ListenableWorker.Result.Success()

        coVerify { traceLocationCleaner.cleanUp() }
    }
}
