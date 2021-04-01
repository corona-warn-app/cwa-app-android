package de.rki.coronawarnapp.worker

import androidx.annotation.VisibleForTesting
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import timber.log.Timber
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class for background work handling
 * The helper uses externalised constants and helper for readability.
 *
 * @see BackgroundConstants
 * @see BackgroundWorkHelper
 */
@Singleton
class BackgroundWorkScheduler @Inject constructor(
    private val backgroundWorkBuilder: BackgroundWorkBuilder,
    private val submissionSettings: SubmissionSettings,
    private val tracingSettings: TracingSettings,
) {

    /**
     * Enum class for work tags
     *
     * @param tag the tag of the worker
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORKER_TAG
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORKER_TAG
     * @see BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER_TAG
     * @see BackgroundConstants.BACKGROUND_NOISE_ONE_TIME_WORKER_TAG
     * @see BackgroundConstants.BACKGROUND_NOISE_PERIODIC_WORKER_TAG
     */
    enum class WorkTag(val tag: String) {
        DIAGNOSIS_KEY_RETRIEVAL_ONE_TIME_WORKER(BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORKER_TAG),
        DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORKER_TAG),
        DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER_TAG),
        BACKGROUND_NOISE_ONE_TIME_WORKER(BackgroundConstants.BACKGROUND_NOISE_ONE_TIME_WORKER_TAG),
        BACKGROUND_NOISE_PERIODIC_WORKER(BackgroundConstants.BACKGROUND_NOISE_PERIODIC_WORKER_TAG)
    }

    /**
     * Enum class for work type
     *
     * @param uniqueName the unique name of specified work
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORK_NAME
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORK_NAME
     * @see BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORK_NAME
     * @see BackgroundConstants.BACKGROUND_NOISE_PERIODIC_WORK_NAME
     * @see BackgroundConstants.BACKGROUND_NOISE_ONE_TIME_WORK_NAME
     */
    enum class WorkType(val uniqueName: String) {
        DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK(BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORK_NAME),
        DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORK_NAME),
        DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORK_NAME),
        BACKGROUND_NOISE_PERIODIC_WORK(BackgroundConstants.BACKGROUND_NOISE_PERIODIC_WORK_NAME),
        BACKGROUND_NOISE_ONE_TIME_WORK(BackgroundConstants.BACKGROUND_NOISE_ONE_TIME_WORK_NAME)
    }

    /**
     * Work manager instance
     */
    private val workManager by lazy { WorkManager.getInstance(CoronaWarnApplication.getAppContext()) }

    /**
     * Start work scheduler
     * Checks if periodic worker was already scheduled. If not - reschedule it again.
     * For [WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER] also checks if User is Registered
     *
     * @see de.rki.coronawarnapp.submission.SubmissionSettings.registrationToken
     * @see isWorkActive
     */
    fun startWorkScheduler() {
        val isPeriodicWorkActive = isWorkActive(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag).also {
            Timber.d("DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER isPeriodicWorkActive=$it")
        }

        if (!isPeriodicWorkActive) {
            WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.start()
            Timber.d("Starting DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK")
        }
        if (!submissionSettings.isSubmissionSuccessful) {
            if (!isWorkActive(WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER.tag) &&
                submissionSettings.registrationToken.value != null &&
                !tracingSettings.isTestResultAvailableNotificationSent
            ) {
                WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.start()
                tracingSettings.initialPollingForTestResultTimeStamp = System.currentTimeMillis()
                Timber.d("Starting DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER")
            }
        }
    }

    /**
     * Stop work by unique name
     *
     * @return Operation
     *
     * @see WorkType
     */
    fun WorkType.stop(): Operation =
        workManager.cancelUniqueWork(this.uniqueName)

    /**
     * Checks if defined work is active
     * Non-active means worker was Cancelled, Failed or have not been enqueued at all
     *
     * @param tag String tag of the worker
     *
     * @return Boolean
     *
     * @see WorkInfo.State.CANCELLED
     * @see WorkInfo.State.FAILED
     */
    private fun isWorkActive(tag: String): Boolean {
        val workStatus = workManager.getWorkInfosByTag(tag)
        var result = true
        try {
            val workInfoList = workStatus.get()
            if (workInfoList.size == 0) result = false
            for (info in workInfoList) {
                if (info.state == WorkInfo.State.CANCELLED || info.state == WorkInfo.State.FAILED) {
                    result = false
                }
            }
        } catch (e: ExecutionException) {
            result = false
        } catch (e: InterruptedException) {
            result = false
        }
        return result
    }

    /**
     * Stop work scheduler
     * Stops all background work by tag.
     */
    fun stopWorkScheduler() {
        WorkTag.values().map { workTag: WorkTag ->
            workManager.cancelAllWorkByTag(workTag.tag)
                .also { it.logOperationCancelByTag(workTag) }
        }
        Timber.d("All Background Jobs Stopped")
    }

    /**
     * Schedule diagnosis key periodic time work
     *
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK
     */
    fun scheduleDiagnosisKeyPeriodicWork() {
        WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.start()
    }

    /**
     * Schedule diagnosis key one time work
     *
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK
     */
    fun scheduleDiagnosisKeyOneTimeWork() {
        WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK.start()
    }

    /**
     * Schedule background noise periodic work
     *
     * @see WorkType.BACKGROUND_NOISE_PERIODIC_WORK
     */
    fun scheduleBackgroundNoisePeriodicWork() {
        WorkType.BACKGROUND_NOISE_PERIODIC_WORK.start()
    }

    fun stopBackgroundNoisePeriodicWork() {
        WorkType.BACKGROUND_NOISE_PERIODIC_WORK.start()
    }

    /**
     * Schedule background noise one time work
     *
     * @see WorkType.BACKGROUND_NOISE_ONE_TIME_WORK
     */
    fun scheduleBackgroundNoiseOneTimeWork() {
        WorkType.BACKGROUND_NOISE_ONE_TIME_WORK.start()
    }

    fun stopDiagnosisTestResultPeriodicWork() {
        WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop()
    }

    /**
     * Enqueue operation for work type defined in WorkType enum class
     *
     * @return Operation
     *
     * @see WorkType
     */
    private fun WorkType.start(): Operation = when (this) {
        WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK -> enqueueDiagnosisKeyBackgroundPeriodicWork()
        WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK -> enqueueDiagnosisKeyBackgroundOneTimeWork()
        WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER -> enqueueDiagnosisTestResultBackgroundPeriodicWork()
        WorkType.BACKGROUND_NOISE_PERIODIC_WORK -> enqueueBackgroundNoisePeriodicWork()
        WorkType.BACKGROUND_NOISE_ONE_TIME_WORK -> enqueueBackgroundNoiseOneTimeWork()
    }

    /**
     * Enqueue diagnosis key periodic work and log it
     * Replace with new if older work exists.
     *
     * @return Operation
     *
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun enqueueDiagnosisKeyBackgroundPeriodicWork() = workManager.enqueueUniquePeriodicWork(
        WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.uniqueName,
        ExistingPeriodicWorkPolicy.REPLACE,
        backgroundWorkBuilder.buildDiagnosisKeyRetrievalPeriodicWork()
    ).also { it.logOperationSchedule(WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK) }

    /**
     * Enqueue diagnosis key one time work and log it
     * Replace with new if older work exists.
     *
     * @return Operation
     *
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK
     */
    private fun enqueueDiagnosisKeyBackgroundOneTimeWork() = workManager.enqueueUniqueWork(
        WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK.uniqueName,
        ExistingWorkPolicy.REPLACE,
        backgroundWorkBuilder.buildDiagnosisKeyRetrievalOneTimeWork()
    ).also { it.logOperationSchedule(WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK) }

    /**
     * Enqueue diagnosis Test Result periodic
     * Show a Notification when new Test Results are in.
     * Replace with new if older work exists.
     *
     * @return Operation
     *
     * @see WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER
     */
    private fun enqueueDiagnosisTestResultBackgroundPeriodicWork() =
        workManager.enqueueUniquePeriodicWork(
            WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            backgroundWorkBuilder.buildDiagnosisTestResultRetrievalPeriodicWork()
        ).also { it.logOperationSchedule(WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER) }

    /**
     * Enqueue background noise periodic
     * Replace with new if older work exists.
     *
     * @return Operation
     *
     * @see WorkType.BACKGROUND_NOISE_PERIODIC_WORK
     */
    private fun enqueueBackgroundNoisePeriodicWork() =
        workManager.enqueueUniquePeriodicWork(
            WorkType.BACKGROUND_NOISE_PERIODIC_WORK.uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            backgroundWorkBuilder.buildBackgroundNoisePeriodicWork()
        ).also { it.logOperationSchedule(WorkType.BACKGROUND_NOISE_PERIODIC_WORK) }

    /**
     * Enqueue background noise one time
     * Replace with new if older work exists.
     *
     * @return Operation
     *
     * @see WorkType.BACKGROUND_NOISE_ONE_TIME_WORK
     */
    private fun enqueueBackgroundNoiseOneTimeWork() =
        workManager.enqueueUniqueWork(
            WorkType.BACKGROUND_NOISE_ONE_TIME_WORK.uniqueName,
            ExistingWorkPolicy.REPLACE,
            backgroundWorkBuilder.buildBackgroundNoiseOneTimeWork()
        ).also { it.logOperationSchedule(WorkType.BACKGROUND_NOISE_ONE_TIME_WORK) }

    /**
     * Log operation schedule
     */
    private fun Operation.logOperationSchedule(workType: WorkType) =
        this.result.addListener(
            { Timber.d("${workType.uniqueName} completed.") },
            { it.run() }
        ).also { Timber.d("${workType.uniqueName} scheduled.") }

    /**
     * Log operation cancellation
     */
    private fun Operation.logOperationCancelByTag(workTag: WorkTag) =
        this.result.addListener(
            { Timber.d("All work with tag ${workTag.tag} canceled.") },
            { it.run() }
        ).also { Timber.d("Canceling all work with tag ${workTag.tag}") }
}
