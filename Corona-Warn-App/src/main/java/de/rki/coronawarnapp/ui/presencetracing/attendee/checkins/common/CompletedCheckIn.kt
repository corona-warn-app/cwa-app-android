package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.format.DateTimeFormat

inline val CheckIn.checkoutInfo: String
    get() {
        val checkInStartUserTZ = checkInStart.toUserTimeZone()
        val checkInEndUserTZ = checkInEnd.toUserTimeZone()

        return if (checkInStartUserTZ.toLocalDate() == checkInEndUserTZ.toLocalDate()) {
            val dateFormat = DateTimeFormat.shortDate()
            val timeFormat = DateTimeFormat.shortTime()
            val dayFormatted = checkInStartUserTZ.toString(dateFormat)
            val startTimeFormatted = checkInStartUserTZ.toString(timeFormat)
            val endTimeFormatted = checkInEndUserTZ.toString(timeFormat)

            String.format("%s, %s - %s", dayFormatted, startTimeFormatted, endTimeFormatted)
        } else {
            val dateTimeFormat = DateTimeFormat.shortDateTime()
            val startTimeFormatted = checkInStartUserTZ.toString(dateTimeFormat)
            val endTimeFormatted = checkInEndUserTZ.toString(dateTimeFormat)

            String.format("%s - %s", startTimeFormatted, endTimeFormatted)
        }
    }
