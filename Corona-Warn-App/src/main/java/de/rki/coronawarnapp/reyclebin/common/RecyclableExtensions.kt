package de.rki.coronawarnapp.reyclebin.common

import org.joda.time.Days
import org.joda.time.Instant

fun Recyclable.retentionDaysInRecycleBin(now: Instant): Int = recycledAt?.let { Days.daysBetween(it, now).days } ?: 0
