package de.rki.coronawarnapp.util

import java.time.temporal.ChronoUnit

fun org.joda.time.LocalDate.toJavaTime(): java.time.LocalDate = java.time.LocalDate.of(year, monthOfYear, dayOfMonth)
fun org.joda.time.Instant.toJavaInstant(): java.time.Instant = java.time.Instant.ofEpochMilli(millis)
fun java.time.Instant.toJodaInstant(): org.joda.time.Instant = org.joda.time.Instant.ofEpochMilli(toEpochMilli())
fun java.time.Duration.toJoda(): org.joda.time.Duration = org.joda.time.Duration(toMillis())
fun org.joda.time.Duration.toJava(): java.time.Duration = java.time.Duration.of(millis, ChronoUnit.MILLIS)
fun java.time.LocalDate.toJodaTime(): org.joda.time.LocalDate = org.joda.time.LocalDate(year, monthValue, dayOfMonth)
