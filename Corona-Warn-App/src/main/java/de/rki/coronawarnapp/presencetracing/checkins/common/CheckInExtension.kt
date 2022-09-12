package de.rki.coronawarnapp.presencetracing.checkins.common

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.toUserTimeZone
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val CheckIn.locationName: String
    get() {
        val nameParts = mutableListOf(description, address)

        @Suppress("ComplexCondition")
        if (traceLocationStart != null &&
            traceLocationEnd != null &&
            traceLocationStart.toEpochMilli() > 0 &&
            traceLocationEnd.toEpochMilli() > 0
        ) {
            val formattedStartDate = traceLocationStart.toUserTimeZone()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

            val formattedEndDate = traceLocationEnd.toUserTimeZone()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            nameParts.add("$formattedStartDate - $formattedEndDate")
        }

        return nameParts.joinToString(separator = ", ")
    }
