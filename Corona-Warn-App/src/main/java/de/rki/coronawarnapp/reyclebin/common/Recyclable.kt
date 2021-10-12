package de.rki.coronawarnapp.reyclebin.common

import org.joda.time.Instant

interface Recyclable {

    /**
     * The time when the user has moved this certificate into the recycle bin
     */
    val recycledAt: Instant?

    /**
     * Indicates if the user has moved this certificate into the recycle bin
     */
    val isRecycled: Boolean
        get() = isRecycled(recycledAt)

    /**
     * Indicates if the user has not moved this certificate into the recycle bin
     */
    val isNotRecycled: Boolean
        get() = !isRecycled(recycledAt)
}

private fun isRecycled(recycledAt: Instant?): Boolean = recycledAt != null
