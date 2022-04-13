package de.rki.coronawarnapp.covidcertificate.revocation.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
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
    // To Do: Implement
    val revocationList: Flow<List<CachedRevocationChunk>> = flowOf<List<CachedRevocationChunk>>(emptyList())
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    suspend fun updateRevocationList(certificates: Set<CwaCovidCertificate>) {
        // Update
    }
}

private val TAG = tag<RevocationRepository>()
