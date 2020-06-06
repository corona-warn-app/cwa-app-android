@file:JvmName("FormatterHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

/*Style*/
/**
 * Formats drawable resource of item to be displayed depending on flag provided
 *
 * @param value
 * @param drawableTrue
 * @param drawableFalse
 * @return
 */
fun formatDrawable(value: Boolean, drawableTrue: Int, drawableFalse: Int): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (value) {
        appContext.getDrawable(drawableTrue)
    } else {
        appContext.getDrawable(drawableFalse)
    }
}

/**
 * Formats color resource of item to be displayed depending on flag provided
 *
 * @param value
 * @param colorTrue
 * @param colorFalse
 * @return
 */
fun formatColor(value: Boolean, colorTrue: Int, colorFalse: Int): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (value) {
        appContext.getColor(colorTrue)
    } else {
        appContext.getColor(colorFalse)
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
fun formatVisibilityIcon(drawable: Any?): Int? {
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

/*Text*/
/**
 * Formats text resource to be displayed depending on flag provided
 *
 * @param value
 * @param stringTrue
 * @param stringFalse
 * @return
 */
fun formatText(value: Boolean?, stringTrue: Int, stringFalse: Int): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (value == true) {
        appContext.getString(stringTrue)
    } else {
        appContext.getString(stringFalse)
    }
}

/**
 * Formats color to be displayed depending on color id provided with default option
 *
 * @param color
 * @return
 */
fun formatColorIcon(color: Int?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return color ?: appContext.getColor(R.color.colorAccentTintIcon)
}

fun formatStringAsHTML(@StringRes stringRes: Int) = HtmlCompat.fromHtml(
    CoronaWarnApplication.getAppContext().getString(stringRes),
    HtmlCompat.FROM_HTML_MODE_LEGACY
)