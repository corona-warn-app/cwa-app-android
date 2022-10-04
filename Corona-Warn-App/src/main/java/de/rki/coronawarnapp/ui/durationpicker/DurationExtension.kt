package de.rki.coronawarnapp.ui.durationpicker

import java.time.Duration

/**
 * Format duration as HH:mm
 */
fun Duration.format(): String {
    return DURATION_FORMAT.format(toHoursPart(), toMinutesPart())
}

fun Duration.toReadableDuration(prefix: String? = null, suffix: String? = null): String {
    return listOfNotNull(prefix, format(), suffix).joinToString(separator = " ")
}

private const val DURATION_FORMAT = "%02d:%02d"
