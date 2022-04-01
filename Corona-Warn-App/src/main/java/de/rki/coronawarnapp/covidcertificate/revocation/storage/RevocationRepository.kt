package de.rki.coronawarnapp.covidcertificate.revocation.storage

import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope
) {

    // TODO("Not yet implemented")

    val revocationList: Flow<List<RevocationEntryCoordinates>> = flowOf<List<RevocationEntryCoordinates>>(emptyList())
        .shareLatest(
            tag = TAG,
            scope = appScope
        )
}

private val TAG = tag<RevocationRepository>()
