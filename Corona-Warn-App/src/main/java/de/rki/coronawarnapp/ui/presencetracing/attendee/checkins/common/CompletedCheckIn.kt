package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.toUserTimeZone
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

inline val CheckIn.checkoutInfo: String
    get() {
        val checkInStartUserTZ = checkInStart.toUserTimeZone()
        val checkInEndUserTZ = checkInEnd.toUserTimeZone()

        return if (checkInStartUserTZ.toLocalDate() == checkInEndUserTZ.toLocalDate()) {
            val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            val dayFormatted = checkInStartUserTZ.format(dateFormat)
            val startTimeFormatted = checkInStartUserTZ.format(timeFormat)
            val endTimeFormatted = checkInEndUserTZ.format(timeFormat)

            String.format("%s, %s - %s", dayFormatted, startTimeFormatted, endTimeFormatted)
        } else {
            val dateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            val startTimeFormatted = checkInStartUserTZ.format(dateTimeFormat)
            val endTimeFormatted = checkInEndUserTZ.format(dateTimeFormat)

            String.format("%s - %s", startTimeFormatted, endTimeFormatted)
        }
    }
