@file:JvmName("FormatterInteroperabilityHelper")

package de.rki.coronawarnapp.util.formatter

fun formatInteroperabilityStatus(selectedCountrySize: Int): String {
    return if (selectedCountrySize > 0) {
        "$selectedCountrySize ausgewählt"
    } else {
        "Aus"
    }
}
