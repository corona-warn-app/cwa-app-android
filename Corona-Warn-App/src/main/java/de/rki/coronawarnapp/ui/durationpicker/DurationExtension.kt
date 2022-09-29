package de.rki.coronawarnapp.ui.durationpicker

import java.time.Duration

fun Duration.toContactDiaryFormat(): String {
    val minutes = toMinutes() - toHours() * 60
    return DURATION_FORMAT.format(toHours(), minutes)
}

fun Duration.toReadableDuration(prefix: String? = null, suffix: String? = null): String {
    val durationInMinutes = toMinutes()
    val durationString = formatDuration(durationInMinutes)
    return listOfNotNull(prefix, durationString, suffix).joinToString(separator = " ")
}

private fun formatDuration(durationInMinutes: Long) =
    DURATION_FORMAT.format(durationInMinutes / 60, (durationInMinutes % 60))

private const val DURATION_FORMAT = "%02d:%02d"
