package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.format.DateTimeFormat

inline val CheckIn.checkoutInfo: String
    get() {
        val checkInStartUserTZ = checkInStart.toUserTimeZone()
        val checkInEndUserTZ = checkInEnd.toUserTimeZone()

        val dayFormatted = checkInStartUserTZ.toLocalDate().toString(DateTimeFormat.mediumDate())
        val startTimeFormatted = checkInStartUserTZ.toLocalTime().toString(DateTimeFormat.shortTime())
        val endTimeFormatted = checkInEndUserTZ.toLocalTime().toString(DateTimeFormat.shortTime())
        return "$dayFormatted, $startTimeFormatted - $endTimeFormatted"
    }
