package de.rki.coronawarnapp.statistics.local.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

class LocalStatisticsConfigStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

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

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences(
                "statistics_local_config",
                Context.MODE_PRIVATE
            )
        } returns mockPreferences
    }

    private fun createInstance() = LocalStatisticsConfigStorage(
        context = context,
        baseGson = SerializationModule().baseGson()
    )

    @Test
    fun `storing two selections works`() {
        val instance = createInstance()

        instance.activeSelections.update {
            locations
        }

        val json = (mockPreferences.dataMapPeek["statistics.local.selections"] as String)

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

        instance.activeSelections.value shouldBe locations
    }

    @Test
    fun `old location format is also parsable`() {
        val instance = createInstance()

        mockPreferences.edit {
            putString("statistics.local.selections", outdatedJSON)
        }

        instance.activeSelections.value shouldBe locations
    }
}
