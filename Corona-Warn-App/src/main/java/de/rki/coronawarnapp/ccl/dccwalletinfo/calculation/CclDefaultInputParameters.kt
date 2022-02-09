package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import android.os.Build
import android.os.LocaleList
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import java.util.Locale

fun getDefaultInputParameters(
    now: DateTime
) = CclInputParameters(
    language = getLanguage(),
    now = CclDateTime(now)
)

data class CclInputParameters(
    val os: String = "android",
    val language: String,
    val now: CclDateTime,
)

data class CclDateTime(
    private val dateTime: DateTime
) {
    private val dateTimeUtc = dateTime.toDateTime(DateTimeZone.UTC)
    val timestamp: Long = dateTime.millis / 1000
    val localDate: String = dateTime.toLocalDateString()
    val localDateTime: String = dateTime.toLocalDateTimeString()
    val localDateTimeMidnight: String = dateTime.toLocalDateTimeMidnightString()
    val utcDate: String = dateTimeUtc.toLocalDateString()
    val utcDateTime: String = dateTimeUtc.toLocalDateTimeString()
    val utcDateTimeMidnight: String = dateTimeUtc.toLocalDateTimeMidnightString()
}

private val supportedLanguages = arrayOf(
    "de",
    "en",
    "bg",
    "pl",
    "ro",
    "tr",
)

private fun getLanguage(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault().getFirstMatch(supportedLanguages)?.language ?: Locale.getDefault().language
    } else {
        Locale.getDefault().language
    }
}

private fun DateTime.toLocalDateString() = toLocalDate().toString()

private fun DateTime.toLocalDateTimeString() = toString(ISODateTimeFormat.dateTimeNoMillis())

private fun DateTime.toLocalDateTimeMidnightString() = withTime(
    0,
    0,
    0,
    0
).toString(ISODateTimeFormat.dateTimeNoMillis())
