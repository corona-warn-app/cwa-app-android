package de.rki.coronawarnapp.transaction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class SubmitDiagnosisCoroutineScope @Inject constructor() : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
}
