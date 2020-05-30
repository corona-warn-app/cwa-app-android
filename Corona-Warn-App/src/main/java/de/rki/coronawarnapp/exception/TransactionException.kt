package de.rki.coronawarnapp.exception

import java.util.UUID

/**
 * An Exception thrown when an error occurs inside the Transaction
 *
 * @param transactionId the atomic Transaction ID
 * @param state the atomic Transaction state (defined in the Transaction) with a valid ToString
 * @param cause the cause of the error
 *
 * @see de.rki.coronawarnapp.transaction.Transaction
 */
class TransactionException constructor(transactionId: UUID, state: String, cause: Throwable?) :
    Exception(
        "An error occurred during execution of transaction $transactionId, State $state",
        cause
    )
