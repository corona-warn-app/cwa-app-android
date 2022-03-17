package de.rki.coronawarnapp.ccl.ui.text

import android.annotation.SuppressLint
import android.content.res.Resources
import android.icu.text.MessageFormat
import android.os.Build
import androidx.annotation.RequiresApi
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.QuantityText
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.lessThanAPILevel
import java.text.ChoiceFormat
import java.util.Locale

@SuppressLint("NewApi")
fun pluralText(
    quantity: Int,
    quantityText: QuantityText,
    locale: Locale
): String = when {
    BuildVersionWrap.lessThanAPILevel(24) -> pluralTextApi23(quantity, quantityText)
    else -> pluralTextApi24(quantity, quantityText, locale)
}

/**
 * Formats Plural text on API 23 using [ChoiceFormat]
 * Note: [ChoiceFormat] is used only to provide a solution for API 23
 * ChoiceFormat does not provide any locale specific behavior
 */
private fun pluralTextApi23(
    quantity: Int,
    text: QuantityText
): String {
    val pattern = "0#${text.zero}" +
        "|1#${text.one}" +
        "|2#${text.two}" +
        "|2<${text.other}"
    return ChoiceFormat(pattern).format(quantity)
}

/**
 * Formats Plural text on API 24+ using [MessageFormat]
 * [MessageFormat] is more convenient than using [Resources.getQuantityString]
 * because [Resources.getQuantityString] formats the string necessary for grammatically correct pluralization
 * which means if the quantity is `zero`, it is not necessary that `zero`'s text value is picked
 * and it returns `other`'s text value
 */
@RequiresApi(Build.VERSION_CODES.N)
private fun pluralTextApi24(
    quantity: Int,
    text: QuantityText,
    locale: Locale
): String {
    val pattern = """
            {0, plural,
            =0{${text.zero}}
            =1{${text.one}}
            =2{${text.two}}
            few{${text.few}}
            many{${text.many}}
            other{${text.other}}}
    """.trimIndent()
    return MessageFormat(pattern, locale).format(arrayOf(quantity))
}
