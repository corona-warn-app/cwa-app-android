@file:JvmName("FormatterInformationLegalHelper")

package de.rki.coronawarnapp.util.formatter

import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.device.DefaultSystemInfoProvider
import java.util.Locale

/**
 * Language based format visibility
 *
 * @param defaultLanguageEnglishOrGerman
 * @param isContactFormView
 * @return
 */

fun formatVisibilityLanguageBased(
    defaultLanguageEnglishOrGerman: Boolean,
    isContactFormView: Boolean?
): Int {
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
    DefaultSystemInfoProvider(CoronaWarnApplication.getAppContext()).locale.also {
        return formatVisibilityLanguageBased(
            it.language == Locale.ENGLISH.language ||
                    it.language == Locale.GERMAN.language, isContactFormView
        )
    }
}
