package de.rki.coronawarnapp.covidcertificate.revocation.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationDataStore
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationReset
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccRevocationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    @BaseJackson private val objectMapper: ObjectMapper,
    @DccRevocationDataStore private val dataStore: DataStore<Preferences>
) {

    val revocationList: Flow<List<CachedRevocationChunk>> = dataStore.data
        .catch {
            Timber.tag(TAG).e(it, "Failed to read DataStore")
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs ->
            prefs[CACHED_REVOCATION_CHUNKS_KEY].toRevocationList()
        }
        .shareLatest(scope = appScope)

    suspend fun saveCachedRevocationChunks(cachedRevocationChunks: Collection<CachedRevocationChunk>) {
        Timber.tag(TAG).d("Saving %d cachedRevocationChunks", cachedRevocationChunks.size)
        val data = cachedRevocationChunks.toList().toJson()
        dataStore.edit { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] = data }
    }

    /**
     * This is only used for testing in DccStateValidationTestViewModel
     * The [DataStore] for revocation gets entirely cleared in [DccRevocationReset]
     **/
    internal suspend fun clear() {
        Timber.tag(TAG).d("Clearing Revocation List.")
        runCatching {
            dataStore.edit { prefs -> prefs.remove(CACHED_REVOCATION_CHUNKS_KEY) }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "Failed to clear Revocation List.")
        }
    }

    private fun String.toCachedRevocationChunks(): List<CachedRevocationChunk> = objectMapper.readValue(this)
    private fun List<CachedRevocationChunk>.toJson(): String = objectMapper.writeValueAsString(this)

    private fun String?.toRevocationList(): List<CachedRevocationChunk> = runCatching {
        this?.toCachedRevocationChunks()
    }
        .onFailure { Timber.tag(TAG).e(it, "Failed to create Revocation List from json=%s", this) }
        .getOrNull() ?: emptyList()
}

private val TAG = tag<DccRevocationRepository>()

@VisibleForTesting
internal val CACHED_REVOCATION_CHUNKS_KEY by lazy {
    stringPreferencesKey("RevocationStorage.revocationListJson")
}
