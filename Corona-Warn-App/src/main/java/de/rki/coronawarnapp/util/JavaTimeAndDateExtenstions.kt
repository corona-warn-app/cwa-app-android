package de.rki.coronawarnapp.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun Instant.toLocalDateTimeUserTz(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
fun Instant.toLocalDateTimeUtc(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneOffset.UTC)
fun Instant.toLocalDateUserTz(): LocalDate = atZone(ZoneId.systemDefault()).toLocalDate()
fun Instant.toLocalDateUtc(): LocalDate = atZone(ZoneOffset.UTC).toLocalDate()
fun LocalDate.ageInDays(now: LocalDate) = ChronoUnit.DAYS.between(this, now).toInt()
