package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.util.GoogleAPIVersion
import javax.inject.Inject
import javax.inject.Singleton

// TODO Remove once we have refactored the transaction and it's no longer a singleton
@Singleton
data class RetrieveDiagnosisInjectionHelper @Inject constructor(
    val transactionScope: TransactionCoroutineScope,
    val googleAPIVersion: GoogleAPIVersion
)
