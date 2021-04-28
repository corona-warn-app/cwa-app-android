package de.rki.coronawarnapp.worker

import java.util.concurrent.TimeUnit

/**
 * The background work constants are used inside the BackgroundWorkScheduler
 */
object BackgroundConstants {

    /**
     * Kind initial delay in minutes for periodic work for accessibility reason
     *
     * @see TimeUnit.MINUTES
     */
    const val KIND_DELAY = 1L

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

    /**
     * The minimum time in hours to wait between playbook executions
     *
     * @see TimeUnit.HOURS
     */
    const val MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION = 0L

    /**
     * The maximum time in hours to wait between playbook executions
     *
     * @see TimeUnit.HOURS
     */
    const val MAX_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION = 0L

    /**
     * The total time in days to run the playbook
     *
     * @see TimeUnit.DAYS
     */
    const val NUMBER_OF_DAYS_TO_RUN_PLAYBOOK = 0
}
