@file:JvmName("FormatterAccessibilityHelper")

package de.rki.coronawarnapp.util.formatter

import de.rki.coronawarnapp.CoronaWarnApplication

fun formatSuffix(string: Int?, suffix: String): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (string != null) {
        "${appContext.getString(string)} $suffix"
    } else ""
}

fun formatTitle(string: Int?): String = formatSuffix(string, "Titel")

fun formatButton(string: Int?): String = formatSuffix(string, "Button")

fun formatImage(string: Int?): String = formatSuffix(string, "Bild")