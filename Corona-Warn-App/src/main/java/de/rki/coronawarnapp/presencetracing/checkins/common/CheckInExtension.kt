package de.rki.coronawarnapp.presencetracing.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import java.time.format.DateTimeFormatter

val CheckIn.locationName: String
    get() {
        val nameParts = mutableListOf(description, address)

        @Suppress("ComplexCondition")
        if (traceLocationStart != null &&
            traceLocationEnd != null &&
            traceLocationStart.toEpochMilli() > 0 &&
            traceLocationEnd.toEpochMilli() > 0
        ) {
            // TODO: use short variant with locale eg.: DateFormat.getDateInstance(DateFormat.DEFAULT, currentLocale)
            val formattedStartDate = traceLocationStart.toUserTimeZone().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val formattedEndDate = traceLocationEnd.toUserTimeZone().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            nameParts.add("$formattedStartDate - $formattedEndDate")
        }

        return nameParts.joinToString(separator = ", ")
    }
