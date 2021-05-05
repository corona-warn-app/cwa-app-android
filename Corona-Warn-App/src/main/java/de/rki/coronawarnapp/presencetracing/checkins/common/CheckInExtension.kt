package de.rki.coronawarnapp.presencetracing.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.format.DateTimeFormat

val CheckIn.locationName: String
    get() {
        val nameParts = mutableListOf(description, address)

        if (traceLocationStart != null && traceLocationEnd != null) {
            if (traceLocationStart.millis > 0 && traceLocationEnd.millis > 0) {
                val formattedStartDate = traceLocationStart.toUserTimeZone().toString(DateTimeFormat.shortDateTime())
                val formattedEndDate = traceLocationEnd.toUserTimeZone().toString(DateTimeFormat.shortDateTime())
                nameParts.add("$formattedStartDate - $formattedEndDate")
            }
        }

        return nameParts.joinToString(separator = ", ")
    }
