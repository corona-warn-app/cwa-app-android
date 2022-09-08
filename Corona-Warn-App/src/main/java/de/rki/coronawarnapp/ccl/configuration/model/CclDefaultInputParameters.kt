package de.rki.coronawarnapp.ccl.configuration.model

import android.os.Build
import android.os.LocaleList
import de.rki.coronawarnapp.BuildConfig
import timber.log.Timber
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun getDefaultInputParameters(
    now: ZonedDateTime
) = CclInputParameters(
    language = cclLanguage,
    now = CclDateTime(now)
)

data class CclInputParameters(
    val os: String = "android",
    val language: String,
    val now: CclDateTime,
)

data class CclDateTime(
    private val dateTime: ZonedDateTime
) {
    private val dateTimeUtc = dateTime.withZoneSameInstant(ZoneOffset.UTC)
    val timestamp: Long = dateTime.toInstant().toEpochMilli() / 1000
    val localDate: String = dateTime.toLocalDateString()
    val localDateTime: String = dateTime.toLocalDateTimeString()
    val localDateTimeMidnight: String = dateTime.toLocalDateTimeMidnightString()
    val utcDate: String = dateTimeUtc.toLocalDateString()
    val utcDateTime: String = dateTimeUtc.toLocalDateTimeString()
    val utcDateTimeMidnight: String = dateTimeUtc.toLocalDateTimeMidnightString()
}

val cclLanguage: String by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault().getFirstMatch(BuildConfig.SUPPORTED_LOCALES)?.language
            ?: Locale.getDefault().language.also {
                Timber.d("No match. Using default language $it")
            }
    } else {
        Locale.getDefault().language
    }.also {
        Timber.d("Language is $it")
    }
}

private fun ZonedDateTime.toLocalDateString() = toLocalDate().toString()

private fun ZonedDateTime.toLocalDateTimeString() =
    truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_DATE_TIME)

private fun ZonedDateTime.toLocalDateTimeMidnightString() =
    truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE_TIME)
