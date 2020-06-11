package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysToMilliseconds
import de.rki.coronawarnapp.util.formatter.TestResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * DiagnosisTestResultRetrievalPeriodicWorker test.
 */
@RunWith(AndroidJUnit4::class)
class DiagnosisTestResultRetrievalPeriodicWorkerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var request: WorkRequest
    private lateinit var request2: WorkRequest
    // small delay because WorkManager does not run work instantly when delay is off
    private val delay = 500L

    @Before
    fun setUp() {
        LocalData.registrationToken("test token")
        LocalData.isTestResultNotificationSent(false)
        mockkObject(LocalData)
        mockkObject(SubmissionService)
        mockkObject(BackgroundWorkScheduler)

        // do not init Test WorkManager instance again between tests
        // leads to all tests instead of first one to fail
        context = ApplicationProvider.getApplicationContext()
        if (WorkManager.getInstance(context) !is TestDriver) {
            WorkManagerTestInitHelper.initializeTestWorkManager(context)
        }
        workManager = WorkManager.getInstance(context)

        every { BackgroundWorkScheduler["buildDiagnosisTestResultRetrievalPeriodicWork"]() } answers {
            request = this.callOriginal() as WorkRequest
            request
        }

        val slot = slot<String>()
        every { BackgroundWorkScheduler["isWorkActive"](capture(slot)) } answers {
            !(slot.isCaptured && slot.captured ==
                    BackgroundWorkScheduler.WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER.tag)
        }
    }

    /**
     * Test worker for running more than a set amount of days.
     *
     * @see [BackgroundConstants.POLLING_VALIDITY_MAX_DAYS]
     */
    @Test
    fun testDiagnosisTestResultRetrievalPeriodicWorkerCancel() {
        val past = System.currentTimeMillis() -
                (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() + 1).daysToMilliseconds()
        testDiagnosisTestResultRetrievalPeriodicWorkerForResult(mockk(), past, true)
    }

    /**
     * Test worker for running less than a set amount of days, [TestResult.PENDING].
     *
     * @see [BackgroundConstants.POLLING_VALIDITY_MAX_DAYS]
     */
    @Test
    fun testDiagnosisTestResultRetrievalPeriodicWorkerPending() {
        val past = Date().time - (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() - 1).daysToMilliseconds()
        testDiagnosisTestResultRetrievalPeriodicWorkerForResult(TestResult.PENDING, past)
    }

    /**
     * Test worker for running less than a set amount of days, [TestResult.NEGATIVE]  .
     *
     * @see [BackgroundConstants.POLLING_VALIDITY_MAX_DAYS]
     */
    @Test
    fun testDiagnosisTestResultRetrievalPeriodicWorkerSuccessNegative() {
        val past = Date().time - (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() - 1).daysToMilliseconds()
        testDiagnosisTestResultRetrievalPeriodicWorkerForResult(TestResult.NEGATIVE, past)
    }

    /**
     * Test worker for running less than a set amount of days, [TestResult.POSITIVE]  .
     *
     * @see [BackgroundConstants.POLLING_VALIDITY_MAX_DAYS]
     */
    @Test
    fun testDiagnosisTestResultRetrievalPeriodicWorkerSuccessPositive() {
        val past = Date().time - (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() - 1).daysToMilliseconds()
        testDiagnosisTestResultRetrievalPeriodicWorkerForResult(TestResult.POSITIVE, past)
    }

    /**
     * Test worker for retries and fail.
     */
    @Test
    fun testDiagnosisTestResultRetrievalPeriodicWorkerRetryAndFail() {
        val past = Date().time - (BackgroundConstants.POLLING_VALIDITY_MAX_DAYS.toLong() - 1).daysToMilliseconds()
        coEvery { SubmissionService.asyncRequestTestResult() } throws Exception("test exception")
        every { LocalData.initialPollingForTestResultTimeStamp() } returns past

        BackgroundWorkScheduler.startWorkScheduler()

        assertThat(request, notNullValue())
        var workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo, notNullValue())
        assertThat(workInfo.state, `is`((WorkInfo.State.ENQUEUED)))

        for (i in 1..BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 2) {
            // run job i times
            runPeriodicJobInitialDelayMet()

            // get job run #i result
            when (i) {
                BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 2 -> {
                    assertThat(request, notNullValue())
                    workInfo = workManager.getWorkInfoById(request.id).get()
                    assertThat(workInfo.runAttemptCount, `is`(0))
                }
                else -> {
                    assertThat(request, notNullValue())
                    workInfo = workManager.getWorkInfoById(request.id).get()
                    assertThat(workInfo.runAttemptCount, `is`(i))
                }
            }
        }
    }

    @After
    fun cleanUp() {
        workManager.cancelAllWork()
        unmockkAll()
    }

    private fun testDiagnosisTestResultRetrievalPeriodicWorkerForResult(
        result: TestResult,
        past: Long,
        isCancelTest: Boolean = false
    ) {
        coEvery { SubmissionService.asyncRequestTestResult() } returns result
        every { LocalData.initialPollingForTestResultTimeStamp() } returns past

        BackgroundWorkScheduler.startWorkScheduler()

        assertThat(request, notNullValue())

        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo, notNullValue())
        assertThat(workInfo.state, `is`((WorkInfo.State.ENQUEUED)))
        runPeriodicJobInitialDelayMet()
        verifyTestResult(result, isCancelTest)
    }

    private fun verifyTestResult(result: TestResult, isCancelTest: Boolean) {
        assertThat(request, notNullValue())
        val workInfo = workManager.getWorkInfoById(request.id).get()
        if (isCancelTest) {
            assertThat(workInfo.state, `is`((WorkInfo.State.CANCELLED)))
            assertThat(LocalData.isTestResultNotificationSent(), `is`(false))
        } else {
            when (result) {
                TestResult.POSITIVE, TestResult.NEGATIVE, TestResult.INVALID -> {
                    assertThat(workInfo.state, `is`((WorkInfo.State.CANCELLED)))
                    assertThat(LocalData.isTestResultNotificationSent(), `is`(true))
                }
                TestResult.PENDING -> {
                    assertThat(workInfo.runAttemptCount, `is`(0))
                    assertThat(workInfo.state, `is`((WorkInfo.State.ENQUEUED)))
                }
            }
        }
    }

    private fun runPeriodicJobInitialDelayMet() {
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        testDriver?.setAllConstraintsMet(request.id)
        testDriver?.setInitialDelayMet(request.id)
        Thread.sleep(delay)
    }
}
