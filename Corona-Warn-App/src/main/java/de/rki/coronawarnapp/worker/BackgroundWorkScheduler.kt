package de.rki.coronawarnapp.worker

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.LocalData
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Singleton class for background work handling
 * The helper uses externalised constants for readability.
 *
 * @see BackgroundConstants
 */
object BackgroundWorkScheduler {

    private val TAG: String? = BackgroundWorkScheduler::class.simpleName

    /**
     * Enum class for work tags
     *
     * @param tag the tag of the worker
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORKER_TAG
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORKER_TAG
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
     */
    enum class WorkType(val uniqueName: String) {
        DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK(BackgroundConstants.DIAGNOSIS_KEY_ONE_TIME_WORK_NAME),
        DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORK_NAME),
        DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_WORK_NAME)
    }

    /**
     * Calculate the time for diagnosis key retrieval periodic work
     *
     * @return Long
     *
     * @see BackgroundConstants.MINUTES_IN_DAY
     * @see getDiagnosisKeyRetrievalMaximumCalls
     */
    private fun getDiagnosisKeyRetrievalPeriodicWorkTimeInterval(): Long =
            (BackgroundConstants.MINUTES_IN_DAY / getDiagnosisKeyRetrievalMaximumCalls()).toLong()

    /**
     * Calculate the time for diagnosis key retrieval periodic work
     *
     * @return Long
     *
     * @see BackgroundConstants.MINUTES_IN_DAY
     * @see getDiagnosisTestResultRetrievalMaximumCalls
     */
    private fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(): Long =
            (BackgroundConstants.MINUTES_IN_DAY / getDiagnosisTestResultRetrievalMaximumCalls()).toLong()

    /**
     * Get maximum calls count to Google API
     *
     * @return Long
     *
     * @see BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_TRIES_PER_DAY
     * @see BackgroundConstants.GOOGLE_API_MAX_CALLS_PER_DAY
     */
    private fun getDiagnosisKeyRetrievalMaximumCalls() =
            BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_TRIES_PER_DAY
                    .coerceAtMost(BackgroundConstants.GOOGLE_API_MAX_CALLS_PER_DAY)

    /**
     * Get maximum calls count in a Day
     *
     * @return Long
     *
     * @see BackgroundConstants.DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY
     * @see BackgroundConstants.GOOGLE_API_MAX_CALLS_PER_DAY
     */
    private fun getDiagnosisTestResultRetrievalMaximumCalls() =
            BackgroundConstants.DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY

    /**
     * Work manager instance
     */
    private val workManager by lazy { WorkManager.getInstance(CoronaWarnApplication.getAppContext()) }

    /**
     * Start work scheduler
     * Checks if periodic worker was already scheduled. If not - reschedule it again.
     *Checks if User is Registered
     *
     * @see LocalData.registrationToken
     * @see isWorkActive
     */
    fun startWorkScheduler() {
        val isPeriodicWorkActive = isWorkActive(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag)
        logWorkActiveStatus(
                WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag,
                isPeriodicWorkActive
        )
        if (!isPeriodicWorkActive) WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.start()
        if (!isWorkActive(WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER.tag)) {
            if (LocalData.registrationToken() != null && !LocalData.isTestResultNotificationSent()) {
                    WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.start()
                    LocalData.initialPollingForTestResultTimeStamp(System.currentTimeMillis())
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
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY
     * @see BackoffPolicy.LINEAR
     */
    private fun buildDiagnosisKeyRetrievalPeriodicWork() =
            PeriodicWorkRequestBuilder<DiagnosisKeyRetrievalPeriodicWorker>(
                    getDiagnosisKeyRetrievalPeriodicWorkTimeInterval(), TimeUnit.MINUTES
            )
                    .addTag(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER.tag)
                    .setConstraints(getConstraintsForDiagnosisKeyPeriodicBackgroundWork())
                    .setInitialDelay(
                            BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY,
                            TimeUnit.MINUTES
                    )
                    .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY,
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
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY
     * @see BackoffPolicy.LINEAR
     */
    private fun buildDiagnosisKeyRetrievalOneTimeWork() =
            OneTimeWorkRequestBuilder<DiagnosisKeyRetrievalOneTimeWorker>()
                    .addTag(WorkTag.DIAGNOSIS_KEY_RETRIEVAL_ONE_TIME_WORKER.tag)
                    .setConstraints(getConstraintsForDiagnosisKeyOneTimeBackgroundWork())
                    .setInitialDelay(
                            DiagnosisKeyRetrievalTimeCalculator.generateDiagnosisKeyRetrievalOneTimeWorkRandomDuration(
                                    DateTime(
                                            Instant.now(),
                                            DateTimeZone.getDefault()
                                    )
                            ), TimeUnit.MINUTES
                    )
                    .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY,
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
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY
     */
    private fun buildDiagnosisTestResultRetrievalPeriodicWork() =
            PeriodicWorkRequestBuilder<DiagnosisTestResultRetrievalPeriodicWorker>(
                    getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(), TimeUnit.MINUTES
            )
                    .addTag(WorkTag.DIAGNOSIS_TEST_RESULT_RETRIEVAL_PERIODIC_WORKER.tag)
                    .setConstraints(getConstraintsForDiagnosisKeyOneTimeBackgroundWork())
                    .setInitialDelay(
                            BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY,
                            TimeUnit.MINUTES
                    ).setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY,
                            TimeUnit.MINUTES
                    )
                    .build()

    /**
     * Constraints for diagnosis key periodic work
     * Do not execute background work if battery on low level.
     *
     * @return Constraints
     */
    private fun getConstraintsForDiagnosisKeyPeriodicBackgroundWork() =
            Constraints.Builder().setRequiresBatteryNotLow(true).build()

    /**
     * Constraints for diagnosis key one time work
     * Requires battery not low and any network connection
     * Mobile data usage is handled on OS level in application settings
     *
     * @return Constraints
     *
     * @see NetworkType.CONNECTED
     */
    private fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork() =
            Constraints
                    .Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

    /**
     * Log operation schedule
     */
    private fun Operation.logOperationSchedule(workType: WorkType) =
            this.result.addListener({
                                        if (BuildConfig.DEBUG) Log.d(
                                                TAG,
                                                "${workType.uniqueName} completed."
                                        )
                                    }, { it.run() })
                    .also { if (BuildConfig.DEBUG) Log.d(TAG, "${workType.uniqueName} scheduled.") }

    /**
     * Log operation cancellation
     */
    private fun Operation.logOperationCancelByTag(workTag: WorkTag) =
            this.result.addListener({
                                        if (BuildConfig.DEBUG) Log.d(
                                                TAG,
                                                "All work with tag ${workTag.tag} canceled."
                                        )
                                    }, { it.run() })
                    .also { if (BuildConfig.DEBUG) Log.d(TAG, "Canceling all work with tag ${workTag.tag}") }

    /**
     * Log work active status
     */
    private fun logWorkActiveStatus(tag: String, active: Boolean) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Work type $tag is active: $active")
    }
}
