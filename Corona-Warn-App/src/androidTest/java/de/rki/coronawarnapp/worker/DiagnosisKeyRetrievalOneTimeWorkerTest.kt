package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker

import androidx.work.testing.TestListenableWorkerBuilder
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * DiagnosisKeyRetrievalOneTimeWorker test.
 */
@RunWith(AndroidJUnit4::class)
class DiagnosisKeyRetrievalOneTimeWorkerTest {
    private lateinit var context: Context
    private lateinit var worker: ListenableWorker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkObject(RetrieveDiagnosisKeysTransaction)
    }

    /**
     * Test worker result = [ListenableWorker.Result.success]
     */
    @Test
    fun testDiagnosisKeyRetrievalOneTimeWorkerSuccess() {
        worker =
            TestListenableWorkerBuilder<DiagnosisKeyRetrievalOneTimeWorker>(context).build()
        coEvery { RetrieveDiagnosisKeysTransaction.start() } just Runs
        val result = worker.startWork().get()
        assertThat(result, `is`(ListenableWorker.Result.success()))
    }

    /**
     * Test worker result = [ListenableWorker.Result.retry]
     */
    @Test
    fun testDiagnosisKeyRetrievalOneTimeWorkerRetry() {
        worker =
            TestListenableWorkerBuilder<DiagnosisKeyRetrievalOneTimeWorker>(context).build()
        coEvery { RetrieveDiagnosisKeysTransaction.start() } throws Exception("test exception")
        val result = worker.startWork().get()
        assertThat(result, `is`(ListenableWorker.Result.retry()))
    }

    /**
     * Test worker result = [ListenableWorker.Result.failure]
     * Check [BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD] for proper attempt count.
     *
     * @see [BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD]
     */
    @Test
    fun testDiagnosisKeyRetrievalOneTimeWorkerFailed() {
        worker =
            TestListenableWorkerBuilder<DiagnosisKeyRetrievalOneTimeWorker>(context).setRunAttemptCount(5).build()
        val result = worker.startWork().get()
        assertThat(result, `is`(ListenableWorker.Result.failure()))
    }
}
