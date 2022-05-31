package de.rki.coronawarnapp.ccl.configuration.model

import android.os.Build
import android.os.LocaleList
import de.rki.coronawarnapp.BuildConfig
import java.time.OffsetDateTime
import timber.log.Timber
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun getDefaultInputParameters(
    now: OffsetDateTime
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
    private val dateTime: OffsetDateTime
) {
    private val dateTimeUtc = OffsetDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC)
    val timestamp: Long = dateTime.toEpochSecond()
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

private fun OffsetDateTime.toLocalDateString() = toLocalDate().toString()

private fun OffsetDateTime.toLocalDateTimeString() = truncatedTo(ChronoUnit.SECONDS)
    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

private fun OffsetDateTime.toLocalDateTimeMidnightString() = withHour(0)
    .withMinute(0)
    .withSecond(0)
    .withNano(0)
    .toLocalDateTimeString()
