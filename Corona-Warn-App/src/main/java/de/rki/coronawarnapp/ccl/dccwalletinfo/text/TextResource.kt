package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import java.util.Locale

/**
 * Formats [CCLText] lazily when accessed
 */
fun textResource(
    cclText: CCLText,
    locale: Locale = Locale.getDefault()
) = lazy { formatCCLText(cclText, locale) }

internal fun formatCCLText(
    cclText: CCLText?,
    locale: Locale
): String? = when (cclText) {
    is PluralText -> cclText.formatPlural(locale)
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
    locale: Locale
): String? {
    val quantity = quantity(locale)
    val quantityText = localizedText[locale.language]
        ?: localizedText[EN]
        ?: localizedText[DE]

    val text = when (quantity) {
        0 -> quantityText?.zero
        1 -> quantityText?.one
        2 -> quantityText?.two
        in 3..4 -> quantityText?.few
        in 5..7 -> quantityText?.many
        else -> quantityText?.other
    }

    return text
        ?.replace("%@", "%s")
        ?.format(*parameters.convertValues(locale))
}

private fun PluralText.quantity(locale: Locale): Any {
    return quantity ?: run {
        val param = parameters[quantityParameterIndex ?: 0]
        when (param.format) {
            Parameters.FormatType.DATE_DIFF_NOW -> when (param.unit) {
                Parameters.UnitType.DAY ->
                    Days.daysBetween(
                        Instant.parse(param.covertValue(locale).toString()),
                        Instant.now()
                    ).days
                else -> param.covertValue(locale)
            }
            else -> param.covertValue(locale)
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
    Parameters.Type.LOCAL_DATE_TIME -> toLocalDateTime()
    Parameters.Type.UTC_DATE -> toUTCDate()
    Parameters.Type.UTC_DATE_TIME -> toUTCDateTime()
}

private fun Parameters.toUTCDateTime(): String {
    return Instant.parse(value.toString()).toString(
        DateTimeFormat.shortDate()
    )
}

private fun Parameters.toUTCDate(): String {
    return Instant.parse(value.toString()).toString(
        DateTimeFormat.shortDate()
    )
}

private fun Parameters.toLocalDateTime(): String {
    return Instant.parse(value.toString()).toString(
        DateTimeFormat.shortDate()
    )
}

private fun Parameters.toLocalDate(locale: Locale): String {
    return Instant.parse(value.toString()).toString(
        DateTimeFormat.shortDate()
    )
}

private fun Parameters.toNumber(): Int = (value as Number).toInt()
private fun Parameters.toBoolean(): Boolean = value as Boolean

private const val EN = "en"
private const val DE = "de"
