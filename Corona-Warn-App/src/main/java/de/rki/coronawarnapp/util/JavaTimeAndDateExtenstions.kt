package de.rki.coronawarnapp.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun Instant.toUserTimeZone(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
//fun Instant.toLocalDateUtc(): LocalDate = atZone(ZoneOffset.UTC).toLocalDate()
