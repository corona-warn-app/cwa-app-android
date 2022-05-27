package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import java.time.format.DateTimeFormatter

// TODO: use right formatting
inline val CheckIn.checkoutInfo: String
    get() {
        val checkInStartUserTZ = checkInStart.toUserTimeZone()
        val checkInEndUserTZ = checkInEnd.toUserTimeZone()

        return if (checkInStartUserTZ.toLocalDate() == checkInEndUserTZ.toLocalDate()) {
            val dateFormat = DateTimeFormatter.ISO_DATE
            val timeFormat = DateTimeFormatter.ISO_TIME
            val dayFormatted = checkInStartUserTZ.format(dateFormat)
            val startTimeFormatted = checkInStartUserTZ.format(timeFormat)
            val endTimeFormatted = checkInEndUserTZ.format(timeFormat)

            String.format("%s, %s - %s", dayFormatted, startTimeFormatted, endTimeFormatted)
        } else {
            val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME
            val startTimeFormatted = checkInStartUserTZ.format(dateTimeFormat)
            val endTimeFormatted = checkInEndUserTZ.format(dateTimeFormat)

            String.format("%s - %s", startTimeFormatted, endTimeFormatted)
        }
    }
