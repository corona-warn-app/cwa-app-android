package de.rki.coronawarnapp.reyclebin.common

import org.joda.time.Days
import org.joda.time.Duration
import org.joda.time.Instant

interface Recyclable {

    /**
     * The time when the user has moved this item into the recycle bin
     */
    val recycledAt: Instant?

    /**
     * Indicates if the user has moved this item into the recycle bin
     */
    val isRecycled: Boolean
        get() = isRecycled(recycledAt)

    /**
     * Indicates if the user has not moved this item into the recycle bin
     */
    val isNotRecycled: Boolean
        get() = !isRecycled(recycledAt)

    companion object {
        val RETENTION_DAYS: Duration = Days.days(30).toStandardDuration()
    }
}

private fun isRecycled(recycledAt: Instant?): Boolean = recycledAt != null
