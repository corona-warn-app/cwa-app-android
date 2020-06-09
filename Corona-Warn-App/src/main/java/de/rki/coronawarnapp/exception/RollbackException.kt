package de.rki.coronawarnapp.exception

import java.util.UUID

/**
 * An Exception thrown when an error occurs inside the Rollback of a Transaction.
 *
 * @param transactionId the atomic Transaction ID
 * @param state the atomic Transaction state (defined in the Transaction) with a valid ToString
 * @param cause the cause of the error
 *
 * @see de.rki.coronawarnapp.transaction.Transaction
 */
class RollbackException(transactionId: UUID, state: String, cause: Throwable?) :
    Exception(
        "An error occurred during rollback of transaction $transactionId, State $state",
        IllegalStateException(
            "the state before the transaction state could not be restored.",
            cause
        )
    )
