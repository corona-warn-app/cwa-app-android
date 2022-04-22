package de.rki.coronawarnapp.covidcertificate.revocation.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.revocation.RevocationDataStore
import de.rki.coronawarnapp.covidcertificate.revocation.RevocationReset
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    @BaseJackson private val objectMapper: ObjectMapper,
    @RevocationDataStore private val dataStore: DataStore<Preferences>
) {

    val revocationList: Flow<List<CachedRevocationChunk>> = dataStore.data
        .catch {
            Timber.tag(TAG).e(it, "Failed to read DataStore")
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs ->
            prefs[CACHED_REVOCATION_CHUNKS_KEY]?.toCachedRevocationChunks() ?: emptyList()
        }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    suspend fun saveCachedRevocationChunks(cachedRevocationChunks: Collection<CachedRevocationChunk>) {
        Timber.tag(TAG).d("Saving %d cachedRevocationChunks", cachedRevocationChunks.size)
        val data = cachedRevocationChunks.toList().toJson()
        dataStore.edit { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] = data }
    }

    /**
     * This is only used for testing in DccStateValidationTestViewModel
     * The [DataStore] for revocation gets entirely cleared in [RevocationReset]
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
}

private val TAG = tag<RevocationRepository>()

@VisibleForTesting
internal val CACHED_REVOCATION_CHUNKS_KEY by lazy {
    stringPreferencesKey("RevocationStorage.revocationListJson")
}
