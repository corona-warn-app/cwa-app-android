package de.rki.coronawarnapp.worker

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.LocalData
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Singleton class for background work handling
 * The helper uses externalised constants and helper for readability.
 *
 * @see BackgroundConstants
 * @see BackgroundWorkHelper
 */
object BackgroundWorkScheduler {

    /**
     * Enum class for work tags
     *
     * @param tag the tag of the worker
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORKER_TAG
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORKER_TAG
     * @see BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER_TAG
     */
    enum class WorkTag(val tag: String) {
        DIAGNOSIS_KEY_RETRIEVAL_ONE_TIME_WORKER(BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORKER_TAG),
        DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORKER_TAG),
        DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER_TAG)
    }

    /**
     * Enum class for work type
     *
     * @param uniqueName the unique name of specified work
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORK_NAME
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORK_NAME
     * @see BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORK_NAME
     */
    enum class WorkType(val uniqueName: String) {
        DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK(BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORK_NAME),
        DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORK_NAME),
        DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORK_NAME)
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
     * @see LocalData.registrationToken
     * @see isWorkActive
     */
    fun startWorkScheduler() {
        val notificationBody = StringBuilder()
        notificationBody.append("Jobs starting: ")
        if (LocalData.numberOfSuccessfulSubmissions() > 0) return
        val isPeriodicWorkActive = isWorkActive(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag)
        logWorkActiveStatus(
            WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag,
            isPeriodicWorkActive
        )
        if (!isPeriodicWorkActive) {
            WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.start()
            notificationBody.append("[DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK] ")
        }
        if (!isWorkActive(WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER.tag) &&
            LocalData.registrationToken() != null && !LocalData.isTestResultNotificationSent()
        ) {
            WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.start()
            LocalData.initialPollingForTestResultTimeStamp(System.currentTimeMillis())
            notificationBody.append("[DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER]")
        }
        BackgroundWorkHelper.sendDebugNotification(
            "Background Job Starting", notificationBody.toString())
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
        BackgroundWorkHelper.sendDebugNotification(
            "All Background Jobs Stopped", "All Background Jobs Stopped")
    }

    /**
     * Schedule diagnosis key one time work
     *
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK
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
    }

    /**
     * Enqueue diagnosis key periodic work and log it
     * Replace with new if older work exists.
     *
     * @return Operation
     *
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK
     */
    private fun enqueueDiagnosisKeyBackgroundPeriodicWork() = workManager.enqueueUniquePeriodicWork(
        WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.uniqueName,
        ExistingPeriodicWorkPolicy.REPLACE,
        buildDiagnosisKeyRetrievalPeriodicWork()
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
        buildDiagnosisKeyRetrievalOneTimeWork()
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
            buildDiagnosisTestResultRetrievalPeriodicWork()
        ).also { it.logOperationSchedule(WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER) }

    /**
     * Build diagnosis key periodic work request
     * Set "kind delay" for accessibility reason.
     * Backoff criteria set to Linear type.
     *
     * @return PeriodicWorkRequest
     *
     * @see WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER
     * @see BackgroundConstants.KIND_DELAY
     * @see BackgroundConstants.BACKOFF_INITIAL_DELAY
     * @see BackoffPolicy.LINEAR
     */
    private fun buildDiagnosisKeyRetrievalPeriodicWork() =
        PeriodicWorkRequestBuilder<DiagnosisKeyRetrievalPeriodicWorker>(
            BackgroundWorkHelper.getDiagnosisKeyRetrievalPeriodicWorkTimeInterval(), TimeUnit.MINUTES
        )
            .addTag(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag)
            .setInitialDelay(
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    /**
     * Build diagnosis key one time work request
     * Set random initial delay for security reason.
     * Backoff criteria set to Linear type.
     *
     * @return OneTimeWorkRequest
     *
     * @see WorkTag.DIAGNOSIS_KEY_RETRIEVAL_ONE_TIME_WORKER
     * @see buildDiagnosisKeyRetrievalOneTimeWork
     * @see BackgroundConstants.BACKOFF_INITIAL_DELAY
     * @see BackoffPolicy.LINEAR
     */
    private fun buildDiagnosisKeyRetrievalOneTimeWork() =
        OneTimeWorkRequestBuilder<DiagnosisKeyRetrievalOneTimeWorker>()
            .addTag(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_ONE_TIME_WORKER.tag)
            .setConstraints(BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork())
            .setInitialDelay(
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    /**
     * Build diagnosis Test Result periodic work request
     * Set "kind delay" for accessibility reason.
     *
     * @return PeriodicWorkRequest
     *
     * @see WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER
     * @see BackgroundConstants.KIND_DELAY
     */
    private fun buildDiagnosisTestResultRetrievalPeriodicWork() =
        PeriodicWorkRequestBuilder<DiagnosisTestResultRetrievalPeriodicWorker>(
            BackgroundWorkHelper.getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(), TimeUnit.MINUTES
        )
            .addTag(WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER.tag)
            .setConstraints(BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork())
            .setInitialDelay(
                BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY,
                TimeUnit.SECONDS
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    /**
     * Log operation schedule
     */
    private fun Operation.logOperationSchedule(workType: WorkType) =
        this.result.addListener({
            Timber.d("${workType.uniqueName} completed.")
            BackgroundWorkHelper.sendDebugNotification(
                "Background Job Started", "${workType.uniqueName} scheduled")
        }, { it.run() })
            .also { if (BuildConfig.DEBUG) Timber.d("${workType.uniqueName} scheduled.") }

    /**
     * Log operation cancellation
     */
    private fun Operation.logOperationCancelByTag(workTag: WorkTag) =
        this.result.addListener({
            Timber.d("All work with tag ${workTag.tag} canceled.")
            BackgroundWorkHelper.sendDebugNotification(
                "Background Job canceled", "${workTag.tag} canceled")
        }, { it.run() })
            .also { if (BuildConfig.DEBUG) Timber.d("Canceling all work with tag ${workTag.tag}") }

    /**
     * Log work active status
     */
    private fun logWorkActiveStatus(tag: String, active: Boolean) {
        if (BuildConfig.DEBUG) Timber.d("Work type $tag is active: $active")
    }
}
