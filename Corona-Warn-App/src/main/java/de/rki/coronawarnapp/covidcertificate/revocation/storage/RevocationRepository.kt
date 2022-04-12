package de.rki.coronawarnapp.covidcertificate.revocation.storage

import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationServer
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    private val revocationServer: RevocationServer
) {

    // To Do: Implement

    val revocationList: Flow<List<CachedRevocationChunk>> = flowOf<List<CachedRevocationChunk>>(emptyList())
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    fun clear() {
        Timber.tag(TAG).d("clear()")
        revocationServer.clearCache()
    }
}

private val TAG = tag<RevocationRepository>()
