package de.rki.coronawarnapp.statistics.local.storage

import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class LocalStatisticsConfigStorageTest : BaseTest() {

    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(SelectedStatisticsLocation::class.java)
                    .registerSubtype(SelectedStatisticsLocation.SelectedDistrict::class.java)
                    .registerSubtype(SelectedStatisticsLocation.SelectedFederalState::class.java)
            )
            .create()
    }

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

        val json = gson.toJson(locations)

        json shouldBe EXPECTED_JSON
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

    companion object {
        const val EXPECTED_JSON =
            "{\"locations\":[{\"type\":\"SelectedDistrict\",\"district\":{\"districtName\":\"Hogwarts\"," +
                "\"districtShortName\":\"HG\",\"districtId\":1,\"federalStateName\":\"Scotland\"," +
                "\"federalStateShortName\":\"SL\",\"federalStateId\":1},\"addedAt\":{\"iMillis\":6969420000}}," +
                "{\"type\":\"SelectedFederalState\",\"federalState\":\"FEDERAL_STATE_BB\"," +
                "\"addedAt\":{\"iMillis\":4206969000}}]}"
    }
}
