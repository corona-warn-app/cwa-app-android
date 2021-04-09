package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import org.joda.time.Duration
import org.joda.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/**
 * Evaluates the default auto-checkout length depending on the current time
 */
@Suppress("ReturnCount")
fun TraceLocation.getDefaultAutoCheckoutLengthInMinutes(now: Instant): Int {

    // min valid value is 00:15h
    val minDefaultAutoCheckOutLengthInMinutes = 15

    // max valid value is 23:45h
    val maxDefaultAutoCheckOutLengthInMinutes = (TimeUnit.HOURS.toMinutes(23) + 45).toInt()

    // for permanent traceLocations, a defaultCheckInLength is always available
    if (defaultCheckInLengthInMinutes != null) {

        if (defaultCheckInLengthInMinutes < 15) {
            return minDefaultAutoCheckOutLengthInMinutes
        }

        if (defaultCheckInLengthInMinutes > maxDefaultAutoCheckOutLengthInMinutes) {
            return maxDefaultAutoCheckOutLengthInMinutes
        }

        return roundToNearest15Minutes(defaultCheckInLengthInMinutes)
    }
    // for temporary traceLocations, the defaultCheckInLength could be empty
    else {

        // For QR-codes generated by CWA, endDate can never be null here. However, a QR code that was created or
        // modified by a third party could have an endDate that is null.
        if (endDate == null) {
            return minDefaultAutoCheckOutLengthInMinutes
        }

        if (now.isAfter(endDate)) {
            return minDefaultAutoCheckOutLengthInMinutes
        }

        val minutesUntilEndDate = Duration(now, endDate).standardMinutes.toInt()

        if (minutesUntilEndDate < minDefaultAutoCheckOutLengthInMinutes) {
            return minDefaultAutoCheckOutLengthInMinutes
        }

        if (minutesUntilEndDate > maxDefaultAutoCheckOutLengthInMinutes) {
            return maxDefaultAutoCheckOutLengthInMinutes
        }

        return roundToNearest15Minutes(minutesUntilEndDate)
    }
}

private fun roundToNearest15Minutes(minutes: Int): Int {
    val roundingStepInMinutes = 15
    return Duration
        .standardMinutes(
            (minutes.toFloat() / roundingStepInMinutes)
                .roundToLong() * roundingStepInMinutes
        )
        .standardMinutes.toInt()
}
