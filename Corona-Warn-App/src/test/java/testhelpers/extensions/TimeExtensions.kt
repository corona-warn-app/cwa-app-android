package testhelpers.extensions

import java.time.Instant
import java.time.ZonedDateTime

fun String.toInstant(): Instant = ZonedDateTime.parse(this).toInstant()
