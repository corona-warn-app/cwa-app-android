package de.rki.coronawarnapp.covidcertificate.revocation.storage

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    @BaseGson private val gson: Gson,
    private val revocationStorage: RevocationStorage
) {

    val revocationList: Flow<List<CachedRevocationChunk>> = revocationStorage.data
        .map { it?.toCachedRevocationChunks() ?: emptyList() }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    suspend fun saveCachedRevocationChunks(cachedRevocationChunks: Collection<CachedRevocationChunk>) {
        Timber.tag(TAG).d("Saving %d cachedRevocationChunks", cachedRevocationChunks.size)
        val data = cachedRevocationChunks.toList().toJson()
        revocationStorage.save(data)
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        revocationStorage.clear()
    }

    private fun String.toCachedRevocationChunks(): List<CachedRevocationChunk> = gson.fromJson(this)
    private fun List<CachedRevocationChunk>.toJson(): String = gson.toJson(this)
}

private val TAG = tag<RevocationRepository>()
