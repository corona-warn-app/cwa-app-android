package de.rki.coronawarnapp.covidcertificate.common.certificate

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat
import timber.log.Timber

internal fun String.formatDate(): String {
    val regex = "T\\d\\d:\\d\\d:\\d\\d(\\+\\d\\d:\\d\\d)?.*".toRegex()
    return this.replace(regex, "")
}

internal fun String.formatDateTime(tz: DateTimeZone = DateTimeZone.getDefault()): String = try {
    val pattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm 'UTC' ZZ")
    DateTime.parse(
        this,
        DateTimeFormatterBuilder()
            .append(ISODateTimeFormat.date())
            .append(ISODateTimeFormat.timeParser().withOffsetParsed())
            .toFormatter()
    )
        .toDateTime(tz)
        .toString(pattern)
        .removeSuffix(":00")
} catch (e: Exception) {
    this
}

internal fun String.parseLocalDate(): LocalDate? = try {
    LocalDate.parse(this, DateTimeFormat.forPattern("yyyy-MM-dd"))
} catch (e: Exception) {
    Timber.e(e, "Malformed date")
    null
}
