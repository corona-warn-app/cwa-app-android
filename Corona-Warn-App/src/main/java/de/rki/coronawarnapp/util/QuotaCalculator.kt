package de.rki.coronawarnapp.util

/**
 * Class to check if a Quota has been reached based on the calculation done inside
 * the Calculator
 *
 */
interface QuotaCalculator<T> {
    var isAboveQuota: Boolean

    /**
     * This function is called to recalculate an old quota score
     */
    fun calculateQuota()

    /**
     * Reset the quota progress
     *
     * @param newProgress new progress towards the quota
     */
    fun resetProgressTowardsQuota(newProgress: T)

    /**
     * Retrieve the current progress towards the quota
     *
     * @return current progress count
     */
    fun getProgressTowardsQuota(): T
}
