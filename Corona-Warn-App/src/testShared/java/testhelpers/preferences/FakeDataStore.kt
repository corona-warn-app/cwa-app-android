package testhelpers.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake [DataStore] to help with unit testing
 * of components that use [DataStore] as dependency
 */
class FakeDataStore : DataStore<Preferences> {

    private val map = mutableMapOf<Preferences.Key<*>, Any>()

    private val mutablePreferences = mockk<MutablePreferences>()
        .apply {
            every { set(any(), any<Any>()) } answers { map[arg(0)] = arg(1) }
            every { clear() } answers { map.clear() }
            every { remove(any<Preferences.Key<*>>()) } answers { map.remove(arg(0)) }
            every { asMap() } answers { map }
        }

    private val preferences = mockk<Preferences>().apply {
        every { contains(any<Preferences.Key<*>>()) } answers { map.containsKey(arg(0)) }
        every { get(any<Preferences.Key<*>>()) } answers { map[arg(0)] }
        every { toPreferences() } answers { mutablePreferences }
        every { toMutablePreferences() } answers { mutablePreferences }
    }

    operator fun get(key: Preferences.Key<*>): Any? {
        return preferences[key]
    }

    override val data: Flow<Preferences>
        get() = flowOf(preferences)

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return transform(preferences)
    }
}
