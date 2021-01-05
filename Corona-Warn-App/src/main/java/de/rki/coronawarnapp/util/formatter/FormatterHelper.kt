@file:JvmName("FormatterHelper")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.html.HtmlParser

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
        appContext.getColorCompat(colorTrue)
    } else {
        appContext.getColorCompat(colorFalse)
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

/**
 * Formats visibility of item if valid spannable is provided
 *
 * @param text
 * @return
 */
fun formatVisibilityText(text: CharSequence?): Int = formatVisibility(text != null && text != "")

/*Text*/
/**
 * Formats text resource to be displayed depending on flag provided
 *
 * @param value
 * @param stringTrue
 * @param stringFalse
 * @return
 */
fun formatText(value: Boolean?, stringTrue: Int, stringFalse: Int?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (value == true) {
        appContext.getString(stringTrue)
    } else {
        if (stringFalse != null) {
            appContext.getString(stringFalse)
        } else ""
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
    return color ?: appContext.getColorCompat(R.color.colorAccentTintIcon)
}

fun parseHtmlFromAssets(context: Context, path: String): Spanned {
    return HtmlParser(context.assets).parseByAssetPath(path)
}

/**
 * Formats divider color depending on resource value
 * Returns colorHairline as default
 *
 * @param color
 * @return
 */
fun formatColorDivider(context: Context, color: Int?): Int {
    return color ?: context.getColorCompat(R.color.colorHairline)
}

/**
 * Returns string if it isn't null, otherwise it returns an empty String
 *
 * @param string
 * @return String
 */
fun formatEmptyString(string: String?): String = string ?: ""
