package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import java.util.Locale

/**
 * Formats [CCLText] lazily when accessed
 */
fun textResource(
    cclText: CCLText,
    locale: Locale = Locale.getDefault()
) = lazy { formatCCLText(cclText, locale.language) }

internal fun formatCCLText(cclText: CCLText?, lang: String): String? = when (cclText) {
    is PluralText -> "TODO: Format plural text"
    is SingleText -> {
        // DO: provide fallback
        val text = cclText.localizedText[lang]
            ?: cclText.localizedText[EN] // Default for other languages
            ?: cclText.localizedText[DE] // Default for EN
        text?.format(cclText.parameters.format())
    }
    else -> null
}

internal fun List<Parameters>.format(): List<Any> = map { parameter ->
    when (parameter.type) {
        Parameters.Type.STRING -> parameter.toString()
        Parameters.Type.NUMBER -> TODO()
        Parameters.Type.BOOLEAN -> TODO()
        Parameters.Type.DATE -> TODO()
        Parameters.Type.LOCAL_DATE -> TODO()
        Parameters.Type.LOCAL_DATE_TIME -> TODO()
        Parameters.Type.UTC_DATE -> TODO()
        Parameters.Type.UTC_DATE_TIME -> TODO()
    }
}

private const val EN = "en"
private const val DE = "de"
