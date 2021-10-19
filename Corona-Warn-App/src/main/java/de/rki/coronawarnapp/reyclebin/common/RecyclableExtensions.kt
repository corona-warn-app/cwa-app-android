package de.rki.coronawarnapp.reyclebin.common

import org.joda.time.Duration
import org.joda.time.Instant

fun Recyclable.retentionTimeInRecycleBin(now: Instant): Duration =
    recycledAt?.let { Duration(it, now) } ?: Duration.ZERO
