package testhelpers.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeDataStore : DataStore<Preferences> {

    private val map = mutableMapOf<Preferences.Key<*>, Any>()

    private val mutablePreferences = mockk<MutablePreferences>().apply {
        every { set(any<Preferences.Key<Any>>(), any<Any>()) } answers { map[arg(0)] = arg(1) }
    }
    private val preferences = mockk<Preferences>().apply {
        every { contains(any<Preferences.Key<*>>()) } answers { map.containsKey(arg(0)) }
        every { get(any<Preferences.Key<*>>()) } answers { map[arg(0)] }
        every { toPreferences() } answers { this@apply }
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
