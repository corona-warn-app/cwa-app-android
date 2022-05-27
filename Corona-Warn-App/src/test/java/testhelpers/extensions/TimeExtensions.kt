package testhelpers.extensions

import java.time.Instant

fun Instant.isAfterOrEqual(other: Instant) = this.toEpochMilli() >= other.toEpochMilli()
