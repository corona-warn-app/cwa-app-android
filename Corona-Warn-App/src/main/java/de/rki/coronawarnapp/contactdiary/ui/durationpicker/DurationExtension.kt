package de.rki.coronawarnapp.contactdiary.ui.durationpicker

import org.joda.time.Duration

fun Duration.toContactDiaryFormat(): String {
    val hours = if (standardHours < 10) {
        "0$standardHours"
    } else {
        standardHours.toString()
    }
    val minutesCleaned = standardMinutes - standardHours * 60
    val minutes = if (minutesCleaned < 10) {
        "0$minutesCleaned"
    } else {
        minutesCleaned.toString()
    }
    return "$hours:$minutes"
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
