package de.rki.coronawarnapp.exception

/**
 * An Exception thrown when an error occurs inside the Rollback of a Transaction.
 *
 * @param action the received action in the BroadcastReceiver
 * @param expected the expected action
 * @param cause the cause of the error
 *
 * @see de.rki.coronawarnapp.receiver.ExposureStateUpdateReceiver
 */
class WrongReceiverException(
    action: String?,
    expected: String,
    cause: Throwable
) : Exception(
    "An error occurred during BroadcastReceiver onReceive function. " +
            "Received action was $action, expected action was $expected",
    cause
)
