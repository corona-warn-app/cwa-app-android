package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import java.util.Locale

/**
 * Formats [CCLText] lazily when accessed
 */
fun textResource(
    context: Context,
    cclText: CCLText,
    locale: Locale = Locale.getDefault()
) = lazy { formatCCLText(context, cclText, locale) }

internal fun formatCCLText(
    context: Context,
    cclText: CCLText?,
    locale: Locale
): String? = when (cclText) {
    is PluralText -> cclText.formatPlural(context, locale)
    is SingleText -> cclText.formatSingle(locale)
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

private fun PluralText.formatPlural(
    context: Context,
    locale: Locale
): String? {
    val quantity = quantity()
    val quantityText = localizedText[locale.language]
        ?: localizedText[EN]
        ?: localizedText[DE]

    val pluralKey = context.resources.getQuantityString(R.plurals.plural_keys, quantity)
    val text = quantityText?.get(pluralKey)

    return text
        ?.replace("%@", "%s")
        ?.format(*parameters.convertValues(locale))
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

private fun List<Parameters>.convertValues(locale: Locale): Array<Any> = map { parameter ->
    parameter.covertValue(locale)
}.toTypedArray()

private fun Parameters.covertValue(
    locale: Locale
) = when (type) {
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
    return Instant.parse(value.toString()).toDateTime()
        .toString(DateTimeFormat.shortDateTime().withLocale(locale))
}

private fun Parameters.toUTCDate(locale: Locale): String {
    return Instant.parse(value.toString()).toDateTime()
        .toString(DateTimeFormat.shortDate().withLocale(locale))
}

private fun Parameters.toLocalDateTime(locale: Locale): String {
    return Instant.parse(value.toString()).toLocalDateTimeUserTz()
        .toString(DateTimeFormat.shortDateTime().withLocale(locale))
}

private fun Parameters.toLocalDate(locale: Locale): String {
    return Instant.parse(value.toString()).toLocalDateTimeUserTz()
        .toString(DateTimeFormat.shortDate().withLocale(locale))
}

private fun Parameters.toNumber(): Int = (value as Number).toInt()
private fun Parameters.toBoolean(): Boolean = value as Boolean

private const val EN = "en"
private const val DE = "de"
