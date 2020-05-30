package de.rki.coronawarnapp.worker

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.TracingRepository
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
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
        DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORKER_TAG)
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
        DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK(BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_WORK_NAME)
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
     * Shared preferences listener
     */
    private var sharedPrefListener: OnSharedPreferenceChangeListener? = null

    /**
     * Work manager instance
     */
    private val workManager by lazy { WorkManager.getInstance(CoronaWarnApplication.getAppContext()) }

    /**
     * Start work scheduler
     * Subscribe shared preferences listener for changes. If any changes regarding background work
     * occurred, then reschedule periodic work or stop it (depends on changes occurred).
     * Two keys are monitored:
     * - preference_background_jonboarding_allowed
     * - preference_mobile_data_allowed
     * @see LocalData.getBackgroundWorkRelatedPreferences()
     */
    fun startWorkScheduler() {
        sharedPrefListener = OnSharedPreferenceChangeListener { _, key ->
            if (LocalData.getBackgroundWorkRelatedPreferences().contains(key)) {
                logSharedPreferencesChange(key)
                checkStart()
            } else if (key == LocalData.getLastFetchDatePreference()) {
                TracingRepository.refreshLastTimeDiagnosisKeysFetchedDate()
            }
        }
        LocalData.getSharedPreferenceInstance().registerOnSharedPreferenceChangeListener(
            sharedPrefListener
        )
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
     * Check start periodic work
     * If background work is enabled, than reschedule it. else - stop it.
     *
     * @see LocalData.isBackgroundJobEnabled()
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK
     */
    fun checkStart() {
        if (LocalData.isBackgroundJobEnabled()) {
            WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK.stop()
            WorkType.DIAGNOSIS_KEY_BACKGROUND_PERIODIC_WORK.start()
        } else {
            stopWorkScheduler()
        }
    }

    /**
     * Schedule diagnosis key one time work
     *
     * @see LocalData.isBackgroundJobEnabled()
     * @see WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK
     */
    fun scheduleDiagnosisKeyOneTimeWork() {
        if (LocalData.isBackgroundJobEnabled()) {
            WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK.start()
        }
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
    }

    /**
     * Stop work by unique name
     *
     * @return Operation
     *
     * @see WorkType
     */
    private fun WorkType.stop(): Operation = workManager.cancelUniqueWork(this.uniqueName)

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
     * Build diagnosis key periodic work request
     * Set "kind delay" for accessibility reason.
     *
     * @return PeriodicWorkRequest
     *
     * @see WorkTag.DIAGNOSIS_KEY_RETRIEVAL_PERIODIC_WORKER
     * @see BackgroundConstants.DIAGNOSIS_KEY_PERIODIC_KIND_DELAY
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
            .build()

    /**
     * Build diagnosis key one time work request
     * Set random initial delay for security reason.
     *
     * @return OneTimeWorkRequest
     *
     * @see WorkTag.DIAGNOSIS_KEY_RETRIEVAL_ONE_TIME_WORKER
     * @see buildDiagnosisKeyRetrievalOneTimeWork
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
     * Depends on current application settings.
     *
     * @return Constraints
     *
     * @see LocalData.isMobileDataEnabled()
     */
    private fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork(): Constraints {
        val builder = Constraints.Builder()
        if (LocalData.isMobileDataEnabled()) {
            if (BuildConfig.DEBUG) Log.d(
                TAG, "${WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK}:" +
                        "$BackgroundConstants.NETWORK_ROAMING_ALLOWED"
            )
            builder.setRequiredNetworkType(NetworkType.CONNECTED)
        } else {
            if (BuildConfig.DEBUG) Log.d(
                TAG, "${WorkType.DIAGNOSIS_KEY_BACKGROUND_ONE_TIME_WORK}:" +
                        "$BackgroundConstants.NETWORK_ROAMING_FORBIDDEN"
            )
            builder.setRequiredNetworkType(NetworkType.NOT_ROAMING)
        }
        return builder.build()
    }

    /**
     * Log operation schedule
     */
    private fun Operation.logOperationSchedule(workType: WorkType) = this.result.addListener({
        if (BuildConfig.DEBUG) Log.d(TAG, "${workType.uniqueName} completed.")
    }, { it.run() }).also { if (BuildConfig.DEBUG) Log.d(TAG, "${workType.uniqueName} scheduled.") }

    /**
     * Log operation cancellation
     */
    private fun Operation.logOperationCancelByTag(workTag: WorkTag) = this.result.addListener({
        if (BuildConfig.DEBUG) Log.d(TAG, "All work with tag ${workTag.tag} canceled.")
    }, { it.run() })
        .also { if (BuildConfig.DEBUG) Log.d(TAG, "Canceling all work with tag ${workTag.tag}") }

    /**
     * Log shared preferences change
     */
    private fun logSharedPreferencesChange(key: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Shared preferences was changed in key: $key")
    }
}
