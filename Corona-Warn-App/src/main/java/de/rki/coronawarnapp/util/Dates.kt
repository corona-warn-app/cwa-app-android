package de.rki.coronawarnapp.util

object Dates {
    private const val MILLIS_PER_DAY = 24 * 3600 * 1000

    fun numberOfDays(t0: Long, t1: Long) = ((t1 - t0) / MILLIS_PER_DAY).toInt()
}
