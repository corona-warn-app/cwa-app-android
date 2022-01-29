package de.rki.coronawarnapp.ccl.ui.text

import android.annotation.SuppressLint
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
 */
private fun pluralTextApi23(
    quantity: Int,
    quantityText: QuantityText
): String {
    val pattern = "0#${quantityText["zero"]}" +
        "|1#${quantityText["one"]}" +
        "|2#${quantityText["two"]}" +
        "|2<${quantityText["other"]}"
    return ChoiceFormat(pattern).format(quantity)
}

/**
 * Formats Plural text on API 24+ using [MessageFormat]
 */
@RequiresApi(Build.VERSION_CODES.N)
private fun pluralTextApi24(
    quantity: Int,
    quantityText: QuantityText,
    locale: Locale
): String {
    val pattern = """
            {quantity, plural,
            =0{${quantityText["zero"]}}
            =1{${quantityText["one"]}}
            =2{${quantityText["two"]}}
            few{${quantityText["few"]}}
            many{${quantityText["many"]}}
            other{${quantityText["other"]}}
            }
    """.trimIndent()
    return MessageFormat(pattern, locale).format(mapOf("quantity" to quantity))
}
