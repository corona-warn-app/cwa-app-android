package de.rki.coronawarnapp.reyclebin.common

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Duration
import java.time.Instant

interface Recyclable {

    /**
     * The time when the user has moved this item into the recycle bin
     */
    val recycledAt: Instant?

    /**
     * Indicates if the user has moved this item into the recycle bin
     */
    @get:JsonIgnore
    val isRecycled: Boolean
        get() = isRecycled(recycledAt)

    /**
     * Indicates if the user has not moved this item into the recycle bin
     */
    @get:JsonIgnore
    val isNotRecycled: Boolean
        get() = !isRecycled(recycledAt)

    companion object {
        val RETENTION_DAYS: Duration = Duration.ofDays(30)
    }
}

private fun isRecycled(recycledAt: Instant?): Boolean = recycledAt != null
