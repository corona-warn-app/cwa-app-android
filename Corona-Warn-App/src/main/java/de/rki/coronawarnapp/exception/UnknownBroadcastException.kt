package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

/**
 * An Exception thrown when an error occurs inside the Rollback of a Transaction.
 *
 * @param action the received action in the BroadcastReceiver
 *
 * @see de.rki.coronawarnapp.receiver.ExposureStateUpdateReceiver
 */
class UnknownBroadcastException(action: String?) : ReportedException(
    ErrorCodes.WRONG_RECEIVER_PROBLEM.code,
    "Our exposure state update receiver received an unknown '$action' type."
)
