package de.rki.coronawarnapp.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun Instant.toUserTimeZone(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
fun Instant.toLocalDateUtc(): LocalDate = atZone(ZoneOffset.UTC).toLocalDate()
fun LocalDate.ageInDays(now: LocalDate) = ChronoUnit.DAYS.between(this, now).toInt()
fun Instant.toLocalDateUserTimeZone(): LocalDate = atZone(ZoneId.systemDefault()).toLocalDate()
