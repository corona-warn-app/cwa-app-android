package de.rki.coronawarnapp.submission.auto

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.task.SubmissionTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoSubmission @Inject constructor(
    private val timeStamper: TimeStamper,
    private val submissionSettings: SubmissionSettings,
    private val workManager: WorkManager,
    private val taskController: TaskController
) {

    private fun List<TaskInfo>.isSubmissionTaskRunning() = any {
        it.taskState.isActive && it.taskState.request.type == SubmissionTask::class
    }

    val isSubmissionRunning = taskController.tasks.map { it.isSubmissionTaskRunning() }

    fun setup() {
        Timber.tag(TAG).v("setup()")

        if (submissionSettings.autoSubmissionEnabled.value) {
            Timber.tag(TAG).i("Fresh app start and auto submission is enabled, updating mode.")

            updateMode(Mode.SUBMIT_ASAP)
        } else {
            Timber.tag(TAG).d("AutoSubmission is disabled.")
        }
    }

    fun updateMode(newMode: Mode) {
        Timber.tag(TAG).i("updateMode(mode=$newMode)")

        when (newMode) {
            Mode.DISABLED -> disableAutoSubmission()
            Mode.MONITOR -> enableAutoSubmission(lastActivity = timeStamper.nowUTC)
            Mode.SUBMIT_ASAP -> enableAutoSubmission(lastActivity = Instant.EPOCH)
        }
    }

    suspend fun runSubmissionNow() {
        Timber.tag(TAG).i("runSubmissionNow()")

        val result = taskController.submitBlocking(
            DefaultTaskRequest(
                type = SubmissionTask::class,
                arguments = SubmissionTask.Arguments(checkUserActivity = false),
                originTag = TAG
            )
        )
        if (result.isSuccessful) {
            Timber.tag(TAG).i("Blocking submission was successful.")
        } else {
            Timber.tag(TAG).w(result.error, "Blocking submission was not successful, enabling auto submission.")
            updateMode(Mode.SUBMIT_ASAP)
        }
    }

    private fun scheduleWorker() {
        Timber.tag(TAG).v("scheduleWorker(): Creating periodic worker request for submission.")

        val request = PeriodicWorkRequestBuilder<SubmissionWorker>(15, TimeUnit.MINUTES).apply {
            addTag(AUTOSUBMISSION_WORKER_TAG)
            setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BackgroundConstants.BACKOFF_INITIAL_DELAY, TimeUnit.MINUTES)
            setConstraints(
                Constraints.Builder().apply {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }.build()
            )
        }.build()

        workManager.enqueueUniquePeriodicWork(AUTOSUBMISSION_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun enableAutoSubmission(lastActivity: Instant) {
        submissionSettings.apply {
            // Setting last activity to EPOCH will skip the user activity period.
            lastSubmissionUserActivityUTC.update { lastActivity }

            // Will trigger worker scheduling
            autoSubmissionEnabled.update { true }
        }

        scheduleWorker()
    }

    private fun disableAutoSubmission() {
        Timber.tag(TAG).v("disableAutoSubmission()")
        workManager.cancelAllWorkByTag(AUTOSUBMISSION_WORKER_TAG)

        submissionSettings.apply {
            autoSubmissionEnabled.update { false }
            lastSubmissionUserActivityUTC.update { Instant.EPOCH }
            autoSubmissionAttemptsCount.update { 0 }
            autoSubmissionAttemptsLast.update { Instant.EPOCH }
        }
    }

    /**
     * User is still active in the submission flow, causes the submission task to skip if within timeout.
     */
    fun updateLastSubmissionUserActivity() {
        Timber.tag(TAG).d("updateLastSubmissionUserActivity()")
        submissionSettings.lastSubmissionUserActivityUTC.update { timeStamper.nowUTC }
    }

    enum class Mode {
        // Default, no data, no submission.
        DISABLED,

        // Data is available, but user may not be finished, submit on timeout
        MONITOR,

        // Data is available, assume user finished, try async submission ASAP, reset user activity timeout.
        SUBMIT_ASAP
    }

    companion object {
        private const val AUTOSUBMISSION_WORKER_TAG = "AutoSubmissionWorker"
        private const val TAG = "AutoSubmission"
    }
}
