package de.rki.coronawarnapp.statistics.local.storage

import android.content.Context
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage.Companion.PKEY_ACTIVE_SELECTIONS
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore

class LocalStatisticsConfigStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var dataStore: FakeDataStore

    private val locations = SelectedLocations()
        .withLocation(
            SelectedStatisticsLocation.SelectedDistrict(
                Districts.District(
                    "Hogwarts",
                    "HG",
                    1,
                    "Scotland",
                    "SL",
                    1
                ),
                Instant.ofEpochSecond(6969420)
            )
        )
        .withLocation(
            SelectedStatisticsLocation.SelectedFederalState(
                PpaData.PPAFederalState.FEDERAL_STATE_BB,
                Instant.ofEpochSecond(4206969)
            )
        )

    private val outdatedJSON = """
            {
              "locations": [
                {
                  "type": "SelectedDistrict",
                  "district": {
                    "districtName": "Hogwarts",
                    "districtShortName": "HG",
                    "districtId": 1,
                    "federalStateName": "Scotland",
                    "federalStateShortName": "SL",
                    "federalStateId": 1
                  },
                  "uniqueID": 1000001,
                  "addedAt": 6969420000
                },
                {
                  "type": "SelectedFederalState",
                  "federalState": "FEDERAL_STATE_BB",
                  "uniqueID": 2000004,
                  "addedAt": 4206969000
                }
              ]
            }
        """.toComparableJsonPretty()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        dataStore = FakeDataStore()
    }

    private fun createInstance() = LocalStatisticsConfigStorage(
        dataStore = dataStore,
        baseGson = SerializationModule().baseGson()
    )

    @Test
    fun `storing two selections works`() = runTest2 {
        val instance = createInstance()

        instance.updateActiveSelections(locations)

        val json = (dataStore[PKEY_ACTIVE_SELECTIONS] as String)

        json.toComparableJsonPretty() shouldBe """
            {
              "locations": [
                {
                  "type": "SelectedDistrict",
                  "district": {
                    "districtName": "Hogwarts",
                    "districtShortName": "HG",
                    "districtId": 1,
                    "federalStateName": "Scotland",
                    "federalStateShortName": "SL",
                    "federalStateId": 1
                  },
                  "addedAt": 6969420000
                },
                {
                  "type": "SelectedFederalState",
                  "federalState": "FEDERAL_STATE_BB",
                  "addedAt": 4206969000
                }
              ]
            }
        """.toComparableJsonPretty()

        instance.activeSelections.first() shouldBe locations
    }

    @Test
    fun `old location format is also parsable`() = runTest2 {
        val instance = createInstance()
        dataStore[PKEY_ACTIVE_SELECTIONS] = outdatedJSON
        instance.activeSelections.first() shouldBe locations
    }
}
