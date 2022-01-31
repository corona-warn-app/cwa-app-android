package de.rki.coronawarnapp.ccl.ui.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.Locale

/**
 * Formats [CCLText] lazily when accessed
 */
fun textResource(
    cclText: CCLText?,
    locale: Locale = Locale.getDefault(),
    now: Instant = Instant.now()
) = lazy { cclText.format(locale, now) }

fun CCLText?.format(
    locale: Locale = Locale.getDefault(),
    now: Instant = Instant.now()
): String? = runCatching {
    when (this) {
        is PluralText -> formatPlural(locale, now)
        is SingleText -> formatSingle(locale, now)
        else -> null
    }
}.getOrElse {
    Timber.w(it, "CCLText.format() failed")
    null
}

private fun SingleText.formatSingle(
    locale: Locale,
    now: Instant
): String? {
    val text = localizedText[locale.language]
        ?: localizedText[EN] // Default for other languages
        ?: localizedText[DE] // Default for EN

    return text
        ?.replace("%@", "%s")
        ?.format(*parameters.convertValues(locale, now))
}

private fun PluralText.formatPlural(
    locale: Locale,
    now: Instant
): String? {
    val quantity = quantity(now)
    val quantityText = localizedText[locale.language]
        ?: localizedText[EN] // Default for other languages
        ?: localizedText[DE] // Default for EN
        ?: return null

    val text = pluralText(quantity, quantityText, locale)
    return text
        .replace("%@", "%s")
        .format(*parameters.convertValues(locale, now))
}

private fun PluralText.quantity(now: Instant): Int = quantity ?: quantityFromIndex(now)

private fun PluralText.quantityFromIndex(now: Instant): Int {
    val param = parameters[quantityParameterIndex ?: 0]
    return when (param.type) {
        Parameters.Type.STRING -> runCatching {
            param.value.toString().toDouble().toInt()
        }.getOrElse {
            Timber.d("Quantity param is malformed param=$param")
            0
        }
        Parameters.Type.NUMBER -> param.toNumber()
        Parameters.Type.BOOLEAN -> 0
        Parameters.Type.DATE,
        Parameters.Type.LOCAL_DATE,
        Parameters.Type.LOCAL_DATE_TIME,
        Parameters.Type.UTC_DATE,
        Parameters.Type.UTC_DATE_TIME -> param.timeDifference(now)
    }
}

private fun List<Parameters>.convertValues(locale: Locale, now: Instant): Array<Any> =
    map { parameter -> parameter.covertValue(locale, now) }.toTypedArray()

private fun Parameters.covertValue(locale: Locale, now: Instant) = when (type) {
    Parameters.Type.STRING -> value.toString()
    Parameters.Type.NUMBER -> toNumber()
    Parameters.Type.BOOLEAN -> toBoolean()
    Parameters.Type.DATE,
    Parameters.Type.LOCAL_DATE ->
        if (format != null) timeDifference(now) else toLocalDate(locale)
    Parameters.Type.LOCAL_DATE_TIME ->
        if (format != null) timeDifference(now) else toLocalDateTime(locale)
    Parameters.Type.UTC_DATE ->
        if (format != null) timeDifference(now) else toUTCDate(locale)
    Parameters.Type.UTC_DATE_TIME ->
        if (format != null) timeDifference(now) else toUTCDateTime(locale)
}

private fun Parameters.toUTCDateTime(locale: Locale): String {
    return runCatching {
        Instant.parse(value.toString()).run {
            val date = toString(DateTimeFormat.shortDate().withLocale(locale))
            val time = toString(DateTimeFormat.shortTime().withLocale(locale))
            "$date, $time"
        }
    }.getOrElse {
        Timber.e(it, "Parameters.toUTCDateTime() failed")
        ""
    }
}

private fun Parameters.toUTCDate(locale: Locale): String {
    return runCatching {
        Instant.parse(value.toString())
            .toString(DateTimeFormat.shortDate().withLocale(locale))
    }.getOrElse {
        Timber.e(it, "Parameters.toUTCDate() failed")
        ""
    }
}

private fun Parameters.toLocalDateTime(locale: Locale): String {
    return runCatching {
        Instant.parse(value.toString()).toLocalDateTimeUserTz().run {
            val date = toString(DateTimeFormat.shortDate().withLocale(locale))
            val time = toString(DateTimeFormat.shortTime().withLocale(locale))
            "$date, $time"
        }
    }.getOrElse {
        Timber.e(it, "Parameters.toLocalDateTime() failed")
        ""
    }
}

private fun Parameters.toLocalDate(locale: Locale): String {
    return runCatching {
        Instant.parse(value.toString()).toLocalDateTimeUserTz()
            .toString(DateTimeFormat.shortDate().withLocale(locale))
    }.getOrElse {
        Timber.e(it, "Parameters.toLocalDate() failed")
        ""
    }
}

private fun Parameters.toNumber(): Int = runCatching {
    (value as Number).toInt()
}.getOrElse {
    Timber.e(it, "Parameters.toNumber() failed")
    0
}

private fun Parameters.toBoolean(): Boolean = runCatching {
    value as Boolean
}.getOrElse {
    Timber.e(it, " Parameters.toBoolean() failed")
    false
}

private fun Parameters.timeDifference(
    now: Instant
) = when (format) {
    Parameters.FormatType.DATE_DIFF_NOW -> when (unit) {
        Parameters.UnitType.DAY ->
            Days.daysBetween(Instant.parse(value.toString()), now).days
        else -> {
            Timber.w("Date ,but no unit defined param=$this")
            // Date, but unit isn't supported yet, Consider it days
            Days.daysBetween(Instant.parse(value.toString()), now).days
        }
    }
    else -> {
        Timber.w("Date, but no format defined param=$this")
        // Date, but format isn't supported yet, Consider it days
        Days.daysBetween(Instant.parse(value.toString()), now).days
    }
}

private const val EN = "en"
private const val DE = "de"
