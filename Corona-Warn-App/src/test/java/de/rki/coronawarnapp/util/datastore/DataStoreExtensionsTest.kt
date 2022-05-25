package de.rki.coronawarnapp.util.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.FakeDataStore

class DataStoreExtensionsTest : BaseIOTest() {

    private lateinit var dataStore: DataStore<Preferences>
    private val stringPref = stringPreferencesKey("string_test_key")
    private val intPref = intPreferencesKey("int_test_key")

    @BeforeEach
    fun setup() {
        dataStore = FakeDataStore()
    }

    @Test
    fun `get and set value`() = runTest {
        val default = "default"
        val newValue = "newValue"
        val newValue2 = "newValue2"

        dataStore.getValueOrNull(stringPref) shouldBe null
        dataStore.getValueOrDefault(stringPref, default) shouldBe default

        dataStore.setValue(stringPref, newValue)
        dataStore.getValueOrNull(stringPref) shouldBe newValue
        dataStore.getValueOrDefault(stringPref, default) shouldBe newValue

        dataStore.setValue(stringPref, newValue2)
        dataStore.getValueOrNull(stringPref) shouldBe newValue2
        dataStore.getValueOrDefault(stringPref, default) shouldBe newValue2
    }

    @Test
    fun `preferences key mapping`() = runTest {
        val ints = listOf(0, 1, 2, 3, 4, 5, 6)

        flow {
            val prefs = emptyPreferences().toMutablePreferences()
            for (i in ints) {
                prefs[intPref] = i
                emit(prefs)
            }
        }.map(intPref).toList() shouldBe ints
    }

    @Test
    fun `preferences key mapping distinctUntilChanged`() = runTest {
        val ints = listOf(0, 0, 1, 1, 2, 2, 2, 3, 4, 5, 6)
        val distinct = ints.distinct()

        flow {
            val prefs = emptyPreferences().toMutablePreferences()
            for (i in ints) {
                prefs[intPref] = i
                emit(prefs)
            }
        }.distinctUntilChanged(intPref).toList() shouldBe distinct
    }

    @Test
    fun `clears all data`() = runTest {
        val stringValue = "stringValue"
        val intValue = 1

        dataStore.setValue(stringPref, stringValue)
        dataStore.setValue(intPref, intValue)

        dataStore.getValueOrNull(stringPref) shouldBe stringValue
        dataStore.getValueOrNull(intPref) shouldBe intValue

        dataStore.clear()

        dataStore.getValueOrNull(stringPref) shouldBe null
        dataStore.getValueOrNull(intPref) shouldBe null
    }
}
