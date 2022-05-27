package de.rki.coronawarnapp.statistics.local.storage

import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty

class SelectedLocationsTest : BaseTest() {
    private val baseGson = SerializationModule().baseGson()

    private val gson by lazy {
        baseGson
            .newBuilder()
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(SelectedStatisticsLocation::class.java)
                    .registerSubtype(SelectedStatisticsLocation.SelectedDistrict::class.java)
                    .registerSubtype(SelectedStatisticsLocation.SelectedFederalState::class.java)
            )
            .create()
    }

    private val expectedJson = """
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

    @Test
    fun `SelectedStatisticsLocation serialization works`() {
        val locations = SelectedLocations()
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

        val json = gson.toJson(locations).toComparableJsonPretty()

        json shouldBe expectedJson
    }

    @Test
    fun `SelectedStatisticsLocation can be serialized and deserialized`() {
        val initialLocations = SelectedLocations()
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

        val json = gson.toJson(initialLocations)

        val resultLocations = gson.fromJson<SelectedLocations>(json)

        initialLocations shouldBe resultLocations
    }
}
