package testhelpers.extensions

import org.joda.time.Instant
import java.time.ZonedDateTime

fun Instant.isAfterOrEqual(other: Instant) = this.millis >= other.millis

fun String.toJavaInstant(): java.time.Instant = ZonedDateTime.parse(this).toInstant()
