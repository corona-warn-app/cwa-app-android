package testhelpers.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * Fake [DataStore] to help with unit testing
 * of components that use [DataStore] as dependency
 */
class FakeDataStore : DataStore<Preferences> {

    private val preferences = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences>
        get() = preferences

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return preferences.updateAndGet { transform(it).toPreferences() }
    }

    operator fun <T> get(key: Preferences.Key<T>): T? {
        return preferences.value[key]
    }
    
    operator fun <T : Any> set(key: Preferences.Key<T>, value: T) {
        preferences.update {
            it.toMutablePreferences()
                .apply { this[key] = value }
                .toPreferences()
        }
    }

    fun reset() {
        preferences.value = emptyPreferences()
    }
}
