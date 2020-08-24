@file:JvmName("FormatterInformationLegalHelper")

package de.rki.coronawarnapp.util.formatter
import android.content.res.Resources
import android.os.Build
import android.view.View
import java.util.Locale

/**
 * Language based format visibility
 *
 * @param defaultLanguageEnglishOrGerman
 * @param isContactFormView
 * @return
 */

fun formatVisibilityLanguageBased(defaultLanguageEnglishOrGerman: Boolean, isContactFormView: Boolean?): Int {
    if (defaultLanguageEnglishOrGerman) {
        return if (isContactFormView == true) {
            View.VISIBLE
        } else View.GONE
    }
    return if (isContactFormView == false) {
        View.VISIBLE
    } else View.GONE
}

/**
 * checks the default language of the device and formats the visibility
 * Returns visibility value
 *
 * @param isContactFormView
 * @return
 */
fun formatContactForm(isContactFormView: Boolean?): Int {
    var locale: Locale = getSysLocale()
    return formatVisibilityLanguageBased(
        locale.language == Locale.ENGLISH.language ||
                locale.language == Locale.GERMAN.language, isContactFormView)
}

/**
 * Returns the device Locale
 * @see Locale
 *
 * @return
 */
fun getSysLocale(): Locale {
    return if (hasAndroidNOrHigher()) {
        @Suppress("NewApi")
        Resources.getSystem().configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        Resources.getSystem().configuration.locale
    }
}

/**
 * checks if the Android version is N (API version 24) or higher
 * Returns boolean value
 *
 * @return
 */
fun hasAndroidNOrHigher(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return true
    }
    return false
}
