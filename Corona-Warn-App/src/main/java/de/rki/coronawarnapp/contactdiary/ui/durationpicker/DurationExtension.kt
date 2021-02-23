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
