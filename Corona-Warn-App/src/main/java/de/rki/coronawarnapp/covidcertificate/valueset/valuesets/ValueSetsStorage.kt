package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsDataStore
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ValueSetsStorage @Inject constructor(
    @BaseGson private val gson: Gson,
    @ValueSetsDataStore private val dataStore: DataStore<Preferences>,
    @AppScope var appScope: CoroutineScope
) {
    private val mutex = Mutex()

    init {
        appScope.launch {
            migration23To24()
        }
    }

    suspend fun load(): ValueSetsContainer = mutex.withLock {
        Timber.tag(TAG).v("load()")
        val valueSetString = dataStore.dataRecovering.distinctUntilChanged(
            key = PKEY_VALUE_SETS_CONTAINER_PREFIX, defaultValue = ""
        ).first()
        return when (valueSetString.isNotEmpty()) {
            true -> gson.fromJson(valueSetString)
            else -> emptyValueSetsContainer
        }.also { loaded -> Timber.v("Loaded value sets container %s", loaded) }
    }

    suspend fun save(value: ValueSetsContainer) = mutex.withLock {
        Timber.tag(TAG).v("save(value=%s)", value)

        dataStore.trySetValue(
            preferencesKey = PKEY_VALUE_SETS_CONTAINER_PREFIX,
            value = gson.toJson(
                value, ValueSetsContainer::class.java
            )
        )
    }

    // Migration from 2.3.x to 2.4.x: Support for more value sets
    private suspend fun migration23To24() {
        Timber.v("Checking for leftover and removing it")

        dataStore.edit { prefs ->
            if (prefs.contains(PKEY_VALUE_SETS_PREFIX)) {
                prefs.remove(PKEY_VALUE_SETS_PREFIX)
            }
        }
    }

    companion object {
        private const val TAG = "ValueSetsStorage"
        @VisibleForTesting
        val PKEY_VALUE_SETS_PREFIX = stringPreferencesKey("valueset")
        @VisibleForTesting
        val PKEY_VALUE_SETS_CONTAINER_PREFIX = stringPreferencesKey("valuesets_container")
    }
}
