package de.rki.coronawarnapp.worker

import java.util.concurrent.TimeUnit

/**
 * The background work constants are used inside the BackgroundWorkScheduler
 *
 * @see BackgroundWorkScheduler
 */
object BackgroundConstants {

    /**
     * Tag for diagnosis key retrieval one time work
     */
    const val DIAGNOSIS_KEY_ONE_TIME_WORKER_TAG = "DIAGNOSIS_KEY_ONE_TIME_WORKER"

    /**
     * Tag for diagnosis key retrieval periodic work
     */
    const val DIAGNOSIS_KEY_PERIODIC_WORKER_TAG = "DIAGNOSIS_KEY_PERIODIC_WORKER"

    /**
     * Tag for background polling tp check test result periodic work
     */
    const val DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER_TAG = "DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER"

    /**
     * Unique name for diagnosis key retrieval one time work
     */
    const val DIAGNOSIS_KEY_ONE_TIME_WORK_NAME = "DiagnosisKeyBackgroundOneTimeWork"

    /**
     * Unique name for diagnosis key retrieval periodic work
     */
    const val DIAGNOSIS_KEY_PERIODIC_WORK_NAME = "DiagnosisKeyBackgroundPeriodicWork"

    /**
     * Unique name for diagnosis test result retrieval periodic work
     */
    const val DIAGNOSIS_TEST_RESULT_PERIODIC_WORK_NAME = "DiagnosisTestResultBackgroundPeriodicWork"

    /**
     * Total minutes in one day
     */
    const val MINUTES_IN_DAY = 1440

    /**
     * Total tries count for diagnosis key retrieval per day
     * Internal requirement
     */
    const val DIAGNOSIS_KEY_RETRIEVAL_TRIES_PER_DAY = 12

    /**
     * Maximum tries count for diagnosis key retrieval per day
     * Google API limit
     */
    const val GOOGLE_API_MAX_CALLS_PER_DAY = 20

    /**
     * Total tries count for diagnosis key retrieval per day
     * Internal requirement
     */
    const val DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY = 12

    /**
     * Kind initial delay in minutes for periodic work for accessibility reason
     *
     * @see TimeUnit.MINUTES
     */
    const val KIND_DELAY = 1L

    /**
     * Kind initial delay in minutes for periodic work for accessibility reason
     *
     * @see TimeUnit.SECONDS
     */
    const val DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY = 10L

    /**
     * Retries before work would set as FAILED
     */
    const val WORKER_RETRY_COUNT_THRESHOLD = 2

    /**
     * The maximum validity in days for keeping Background polling active
     *
     * @see TimeUnit.DAYS
     */
    const val POLLING_VALIDITY_MAX_DAYS = 21

    /**
     * Backoff initial delay
     *
     * @see TimeUnit.MINUTES
     */
    const val BACKOFF_INITIAL_DELAY = 8L
}
