@file:JvmName("FormatterInformationLegalHelper")

package de.rki.coronawarnapp.util.formatter
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
fun formatContactForm(isContactFormView: Boolean?): Int =
    formatVisibilityLanguageBased(Locale.getDefault().language == Locale.ENGLISH.language ||
        Locale.getDefault().language == Locale.GERMAN.language, isContactFormView)
