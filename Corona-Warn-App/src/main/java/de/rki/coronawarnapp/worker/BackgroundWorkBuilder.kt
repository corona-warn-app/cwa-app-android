package de.rki.coronawarnapp.worker

import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.WorkTag
import java.util.concurrent.TimeUnit

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
fun buildDiagnosisKeyRetrievalPeriodicWork() =
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
fun buildDiagnosisKeyRetrievalOneTimeWork() =
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
fun buildDiagnosisTestResultRetrievalPeriodicWork() =
    PeriodicWorkRequestBuilder<DiagnosisTestResultRetrievalPeriodicWorker>(
        BackgroundWorkHelper.getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(),
        TimeUnit.MINUTES
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
 * Build background noise one time work request
 * Set BackgroundNoiseOneTimeWorkDelay for timing randomness.
 *
 * @return PeriodicWorkRequest
 *
 * @see WorkTag.BACKGROUND_NOISE_ONE_TIME_WORKER
 * @see BackgroundWorkHelper.getBackgroundNoiseOneTimeWorkDelay
 */
fun buildBackgroundNoiseOneTimeWork() =
    OneTimeWorkRequestBuilder<BackgroundNoiseOneTimeWorker>()
        .addTag(WorkTag.BACKGROUND_NOISE_ONE_TIME_WORKER.tag)
        .setConstraints(BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork())
        .setInitialDelay(
            BackgroundWorkHelper.getBackgroundNoiseOneTimeWorkDelay(),
            TimeUnit.HOURS
        ).setBackoffCriteria(
            BackoffPolicy.LINEAR,
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .build()

/**
 * Build background noise periodic work request
 * Set "kind delay" for accessibility reason.
 *
 * @return PeriodicWorkRequest
 *
 * @see BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
 * @see WorkTag.BACKGROUND_NOISE_PERIODIC_WORKER
 * @see BackgroundConstants.KIND_DELAY
 */
fun buildBackgroundNoisePeriodicWork() =
    PeriodicWorkRequestBuilder<BackgroundNoisePeriodicWorker>(
        BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION,
        TimeUnit.HOURS
    )
        .addTag(WorkTag.BACKGROUND_NOISE_PERIODIC_WORKER.tag)
        .setInitialDelay(
            BackgroundConstants.KIND_DELAY,
            TimeUnit.SECONDS
        ).setBackoffCriteria(
            BackoffPolicy.LINEAR,
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .build()
