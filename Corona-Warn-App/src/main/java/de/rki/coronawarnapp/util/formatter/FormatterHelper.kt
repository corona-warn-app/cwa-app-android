@file:JvmName("FormatterHelper")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.text.Spanned
import android.view.View
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.html.HtmlParser

/*Style*/
/**
 * Formats color resource of item to be displayed depending on flag provided
 *
 * @param value
 * @param colorTrue
 * @param colorFalse
 * @return
 */
fun formatColor(context: Context, value: Boolean, colorTrue: Int, colorFalse: Int): Int {
    return if (value) {
        context.getColorCompat(colorTrue)
    } else {
        context.getColorCompat(colorFalse)
    }
}

/**
 * Formats visibility of item depending on flag provided
 *
 * @param value
 * @return
 */
fun formatVisibility(value: Boolean): Int {
    return if (value) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * Formats visibility of item if valid drawable is provided
 *
 * @param drawable
 * @return
 */
fun formatVisibilityIcon(drawable: Any?): Int {
    return if (drawable != null) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * Formats visibility of item depending on flag provided
 * Flag is inverted
 *
 * @param value
 * @return
 */
fun formatVisibilityInverted(value: Boolean): Int = formatVisibility(!value)

/**
 * Formats visibility of item if valid text is provided
 *
 * @param text
 * @return
 */
fun formatVisibilityText(text: String?): Int = formatVisibility(text != null && text != "")

/**
 * Formats visibility of item if valid spannable is provided
 *
 * @param text
 * @return
 */
fun formatVisibilityText(text: CharSequence?): Int = formatVisibility(text != null && text != "")

fun parseHtmlFromAssets(context: Context, path: String): Spanned {
    return HtmlParser(context.assets).parseByAssetPath(path)
}
