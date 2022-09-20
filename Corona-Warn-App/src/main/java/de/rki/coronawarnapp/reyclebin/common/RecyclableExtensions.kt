package de.rki.coronawarnapp.reyclebin.common

import java.time.Duration
import java.time.Instant

fun Recyclable.retentionTimeInRecycleBin(now: Instant): Duration =
    recycledAt?.let { Duration.between(it, now) } ?: Duration.ZERO
