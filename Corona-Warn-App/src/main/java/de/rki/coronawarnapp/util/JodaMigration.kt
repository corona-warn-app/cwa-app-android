package de.rki.coronawarnapp.util

fun org.joda.time.LocalDate.toJavaTime(): java.time.LocalDate = java.time.LocalDate.of(year, monthOfYear, dayOfMonth)
fun org.joda.time.Instant.toJavaInstant(): java.time.Instant = java.time.Instant.ofEpochMilli(millis)
