package de.rki.coronawarnapp.util

/**
 * Class to check if a Quota has been reached based on the calculation done inside
 * the Calculator
 *
 */
interface QuotaCalculator {
    fun isAboveQuota(): Boolean
}
