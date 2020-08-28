package de.rki.coronawarnapp.transaction

import javax.inject.Inject
import javax.inject.Singleton

// TODO Remove once we have refactored the transaction and it's no longer a singleton
@Singleton
data class SubmitDiagnosisInjectionHelper @Inject constructor(
    val transactionScope: SubmitDiagnosisCoroutineScope
)
