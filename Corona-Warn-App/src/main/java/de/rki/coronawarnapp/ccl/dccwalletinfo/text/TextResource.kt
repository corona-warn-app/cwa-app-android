package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import java.util.Locale

fun textResource(
    cclText: CCLText,
    locale: Locale = Locale.getDefault()
) = lazy { formatCCLText(cclText, locale.language) }

private fun formatCCLText(cclText: CCLText, lang: String): String = when (cclText) {
    is PluralText -> "TODO: Format plural text"
    is SingleText -> {
        val text = cclText.localizedText[lang]
            ?: cclText.localizedText[EN] // Default for other languages
            ?: cclText.localizedText[DE] // Default for EN
        text!!.format(cclText.parameters) + lang
    }
}

private const val EN = "en"
private const val DE = "en"
