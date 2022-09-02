package de.rki.coronawarnapp.ui.durationpicker

import org.joda.time.Duration

fun Duration.toContactDiaryFormat(): String {
    val minutes = standardMinutes - standardHours * 60
    return "%02d:%02d".format(standardHours, minutes)
}

fun java.time.Duration.toContactDiaryFormat(): String {
    val minutes = toMinutes() - toHours() * 60
    return "%02d:%02d".format(toHours(), minutes)
}

// returns readable durations with optional prefix and suffix such as "Dauer 01:30 h"
fun Duration.toReadableDuration(prefix: String? = null, suffix: String? = null): String {
    val durationInMinutes = standardMinutes
    val durationString = String.format("%02d:%02d", durationInMinutes / 60, (durationInMinutes % 60))

    return listOfNotNull(
        prefix,
        durationString,
        suffix
    ).joinToString(separator = " ")
}

fun java.time.Duration.toReadableDuration(prefix: String? = null, suffix: String? = null): String {
    val durationInMinutes = toMinutes()
    val durationString = String.format("%02d:%02d", durationInMinutes / 60, (durationInMinutes % 60))

    return listOfNotNull(
        prefix,
        durationString,
        suffix
    ).joinToString(separator = " ")
}
