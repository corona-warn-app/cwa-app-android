package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDateTime
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

internal fun String.formatDate(): String {
    val regex = "T\\d\\d:\\d\\d:\\d\\d(\\+\\d\\d:\\d\\d)?.*".toRegex()
    return this.replace(regex, "")
}

// TODO: test if it's working
internal fun String.formatDateTime(tz: ZoneOffset = OffsetDateTime.now().offset): String = try {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC' ZZ")
    OffsetDateTime.parse(
        this,
        DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_DATE)
            .append(DateTimeFormatter.ISO_OFFSET_TIME)
            .toFormatter()
    )
        .toDateTime(tz)
        .format(pattern)
        .removeSuffix(":00")
} catch (e: Exception) {
    this
}

internal fun String.parseLocalDate(): LocalDate? = try {
    LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
} catch (e: Exception) {
    Timber.e(e, "Malformed date")
    null
}

internal fun String.parseInstant() = try {
    Instant.parse(this)
} catch (e: Exception) {
    Timber.e(e, "Malformed instant")
    null
}
