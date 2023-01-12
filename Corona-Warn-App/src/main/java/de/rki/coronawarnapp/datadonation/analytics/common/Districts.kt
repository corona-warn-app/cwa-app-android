package de.rki.coronawarnapp.datadonation.analytics.common

import android.content.Context
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseJackson
import timber.log.Timber
import javax.inject.Inject

@Reusable
class Districts @Inject constructor(
    @AppContext private val context: Context,
    @BaseJackson private val mapper: ObjectMapper,
) {
    fun loadDistricts(): List<District> {
        return try {
            val rawDistricts = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            mapper.readValue(rawDistricts)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse districts.")
            emptyList()
        }
    }

    data class District(
        @JsonProperty("districtName") val districtName: String = "",
        @JsonProperty("districtShortName") val districtShortName: String = "",
        @JsonProperty("districtId") val districtId: Int = 0,
        @JsonProperty("federalStateName") val federalStateName: String = "",
        @JsonProperty("federalStateShortName") val federalStateShortName: String = "",
        @JsonProperty("federalStateId") val federalStateId: Int = 0
    )

    companion object {
        private const val ASSET_NAME = "ppdd-ppa-administrative-unit-set.json"
        private const val TAG = "Districts"
    }
}
