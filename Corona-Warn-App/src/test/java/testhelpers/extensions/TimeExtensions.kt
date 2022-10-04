package testhelpers.extensions

import java.time.Instant
import java.time.ZonedDateTime

fun String.toJavaInstant(): Instant = ZonedDateTime.parse(this).toInstant()
