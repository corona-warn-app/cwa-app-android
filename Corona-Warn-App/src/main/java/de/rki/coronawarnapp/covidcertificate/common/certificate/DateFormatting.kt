package de.rki.coronawarnapp.covidcertificate.common.certificate

import java.time.LocalDate
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

internal fun String.formatDate(): String {
    val regex = "T\\d\\d:\\d\\d:\\d\\d(\\+\\d\\d:\\d\\d)?.*".toRegex()
    return this.replace(regex, "")
}

internal fun String.formatDateTime(tz: ZoneId = ZoneId.systemDefault()): String {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC' X")
    return try {
        OffsetDateTime.parse(this).atZoneSameInstant(tz).format(pattern)
    } catch (e: Exception) {
        try {
            OffsetDateTime.parse(this, OFFSET_DATE_TIME_FORMATTER).atZoneSameInstant(tz).format(pattern)
        } catch (e: Exception) {
            this
        }
    }
}

internal fun String.parseLocalDate(): LocalDate? = try {
    LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
} catch (e: Exception) {
    Timber.e(e, "Malformed date")
    null
}

internal fun String.parseInstant() = try {
    OffsetDateTime.parse(this).toInstant()
} catch (e: Exception) {
    Timber.e(e, "Malformed instant")
    null
}

private val OFFSET_DATE_TIME_FORMATTER = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_DATE_TIME)
    .append(DateTimeFormatter.ofPattern("X"))
    .toFormatter()
