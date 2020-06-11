@file:JvmName("FormatterAccessibilityHelper")

package de.rki.coronawarnapp.util.formatter

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

fun formatSuffix(string: String?, suffix: Int): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (string != null) {
        "$string ${appContext.getString(suffix)}"
    } else ""
}

fun formatButton(string: String?): String = formatSuffix(string, R.string.suffix_button)

fun formatImage(string: String?): String = formatSuffix(string, R.string.suffix_image)
