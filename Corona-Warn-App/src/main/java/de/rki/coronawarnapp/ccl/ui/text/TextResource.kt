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
    locale: Locale = Locale.getDefault()
) = lazy { cclText.format(locale) }

fun CCLText?.format(
    locale: Locale = Locale.getDefault()
): String? = when (this) {
    is PluralText -> formatPlural(locale)
    is SingleText -> formatSingle(locale)
    else -> null
}

private fun SingleText.formatSingle(
    locale: Locale
): String? {
    val text = localizedText[locale.language]
        ?: localizedText[EN] // Default for other languages
        ?: localizedText[DE] // Default for EN

    return text
        ?.replace("%@", "%s")
        ?.format(*parameters.convertValues(locale))
}

private fun PluralText.formatPlural(locale: Locale): String? {
    val quantity = quantity()
    val quantityText = localizedText[locale.language]
        ?: localizedText[EN] // Default for other languages
        ?: localizedText[DE] // Default for EN
        ?: return null

    val text = pluralText(quantity, quantityText, locale)
    return text
        .replace("%@", "%s")
        .format(*parameters.convertValues(locale))
}

private fun PluralText.quantity(): Int {
    return quantity ?: run {
        val param = parameters[quantityParameterIndex ?: 0]
        when (param.format) {
            Parameters.FormatType.DATE_DIFF_NOW -> when (param.unit) {
                Parameters.UnitType.DAY ->
                    Days.daysBetween(
                        Instant.parse(param.value.toString()),
                        Instant.now()
                    ).days
                else -> param.toNumber()
            }
            else -> param.toNumber()
        }
    }
}

private fun List<Parameters>.convertValues(locale: Locale): Array<Any> =
    map { parameter -> parameter.covertValue(locale) }.toTypedArray()

private fun Parameters.covertValue(locale: Locale) = when (type) {
    Parameters.Type.STRING -> value.toString()
    Parameters.Type.NUMBER -> toNumber()
    Parameters.Type.BOOLEAN -> toBoolean()
    Parameters.Type.DATE,
    Parameters.Type.LOCAL_DATE -> toLocalDate(locale)
    Parameters.Type.LOCAL_DATE_TIME -> toLocalDateTime(locale)
    Parameters.Type.UTC_DATE -> toUTCDate(locale)
    Parameters.Type.UTC_DATE_TIME -> toUTCDateTime(locale)
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

private const val EN = "en"
private const val DE = "de"
