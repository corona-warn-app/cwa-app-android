package testhelpers.extensions

import org.joda.time.Instant

fun Instant.isAfterOrEqual(other: Instant) = this.millis >= other.millis
