package de.rki.coronawarnapp.util.formatter

/**
 * This stati are used for the first row in the main fragment and in the tracing settings,
 * to change the ui depending on the different system stati.
 */
object TracingStatusHelper {
    const val TRACING_ACTIVE = 0
    const val TRACING_INACTIVE = 1
    const val BLUETOOTH = 2
    const val LOCATION = 3
}

/**
 * The following code decides in which state the ui has to be in.
 * This follows this prioritization: Tracing, Location, Bluetooth, Connection
 *
 * @param tracing
 * @param bluetooth
 * @param location
 * @return Int
 */
fun tracingStatusHelper(tracing: Boolean, bluetooth: Boolean, location: Boolean): Int {
    return if (!tracing) {
        TracingStatusHelper.TRACING_INACTIVE
    } else if (!location) {
        TracingStatusHelper.LOCATION
    } else if (!bluetooth) {
        TracingStatusHelper.BLUETOOTH
    } else {
        TracingStatusHelper.TRACING_ACTIVE
    }
}
