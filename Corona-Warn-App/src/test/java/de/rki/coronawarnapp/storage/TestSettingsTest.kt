package de.rki.coronawarnapp.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

class TestSettingsTest : BaseTest() {

    private val objectMapper = SerializationModule().jacksonObjectMapper()
    private lateinit var dataStore: DataStore<Preferences>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        dataStore = FakeDataStore()
    }

    private fun buildInstance(): TestSettings = TestSettings(
        dataStore = dataStore,
        objectMapper = objectMapper
    )
}
