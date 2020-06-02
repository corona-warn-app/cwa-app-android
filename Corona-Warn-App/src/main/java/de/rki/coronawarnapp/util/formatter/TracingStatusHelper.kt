package de.rki.coronawarnapp.util.formatter

/**
 * This stati are used for the first row in the main fragment and in the tracing settings,
 * to change the ui depending on the different system stati.
 */
object TracingStatusHelper {
    const val TRACING_ACTIVE = 0
    const val TRACING_INACTIVE = 1
    const val BLUETOOTH = 2
    const val CONNECTION = 3
}

/**
 * The following table explains the different stati which can appear in the ui.
 * This follows this prioritization: Tracing, Bluetooth, Connection
 * Connection will only be relevant in one exact case, Bluetooth is relevant in two different cases,
 * but independently from the Connection status. And in every other case Tracing will be shown.
 *
 * | Tracing | Bluetooth | Connection | Result     |
 * |---------|-----------|------------|------------|
 * | OFF     | OFF       | ON         | TRACING*   |
 * | OFF     | ON        | OFF        | TRACING*   |
 * | OFF     | ON        | ON         | TRACING*   |
 * | OFF     | OFF       | OFF        | TRACING*   |
 * | ON      | ON        | ON         | TRACING    |
 * | ON      | OFF       | ON         | BLUETOOTH  |
 * | ON      | OFF       | OFF        | BLUETOOTH  |
 * | ON      | ON        | OFF        | CONNECTION |
 * *circle has to be disabled via another formatter
 *
 * @param tracing
 * @param bluetooth
 * @param connection
 * @return Int
 */
fun tracingStatusHelper(tracing: Boolean, bluetooth: Boolean, connection: Boolean): Int {
    return if (tracing && bluetooth && !connection) {
        TracingStatusHelper.CONNECTION
    } else if (tracing && !bluetooth) {
        TracingStatusHelper.BLUETOOTH
    } else if (tracing) {
        TracingStatusHelper.TRACING_ACTIVE
    } else {
        TracingStatusHelper.TRACING_INACTIVE
    }
}
