@file:JvmName("FormatterSettingsHelper")

package de.rki.coronawarnapp.util.formatter

import de.rki.coronawarnapp.R

fun formatStatus(value: Boolean): String = formatText(
    value,
    R.string.settings_on,
    R.string.settings_off
)
