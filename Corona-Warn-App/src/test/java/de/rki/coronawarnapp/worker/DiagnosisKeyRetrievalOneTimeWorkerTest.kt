package de.rki.coronawarnapp.worker

/**
 * DiagnosisKeyRetrievalOneTimeWorker test.
 */
class DiagnosisKeyRetrievalOneTimeWorkerTest {
//    private lateinit var context: Context
//    private lateinit var worker: ListenableWorker
//
//    @Before
//    fun setUp() {
//        context = ApplicationProvider.getApplicationContext()
//        mockkObject(RetrieveDiagnosisKeysTransaction)
//    }
//
//    /**
//     * Test worker result = [ListenableWorker.Result.success]
//     */
//    @Test
//    fun testDiagnosisKeyRetrievalOneTimeWorkerSuccess() {
//        worker =
//            TestListenableWorkerBuilder<DiagnosisKeyRetrievalOneTimeWorker>(context).build()
//        coEvery { RetrieveDiagnosisKeysTransaction.start() } just Runs
//        val result = worker.startWork().get()
//        assertThat(result, `is`(ListenableWorker.Result.success()))
//    }
//
//    /**
//     * Test worker result = [ListenableWorker.Result.retry]
//     */
//    @Test
//    fun testDiagnosisKeyRetrievalOneTimeWorkerRetry() {
//        worker =
//            TestListenableWorkerBuilder<DiagnosisKeyRetrievalOneTimeWorker>(context).build()
//        coEvery { RetrieveDiagnosisKeysTransaction.start() } throws Exception("test exception")
//        val result = worker.startWork().get()
//        assertThat(result, `is`(ListenableWorker.Result.retry()))
//    }
//
//    /**
//     * Test worker result = [ListenableWorker.Result.failure]
//     * Check [BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD] for proper attempt count.
//     *
//     * @see [BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD]
//     */
//    @Test
//    fun testDiagnosisKeyRetrievalOneTimeWorkerFailed() {
//        worker =
//            TestListenableWorkerBuilder<DiagnosisKeyRetrievalOneTimeWorker>(context).setRunAttemptCount(5).build()
//        val result = worker.startWork().get()
//        assertThat(result, `is`(ListenableWorker.Result.failure()))
//    }
}
