package de.rki.coronawarnapp.datadonation.analytics.common

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject

@Reusable
class Districts @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {
    fun loadDistricts(): List<District> {
        return try {
            val rawDistricts = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            gson.fromJson(rawDistricts)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse districts.")
            emptyList()
        }
    }

    data class District(
        @SerializedName("districtName") val districtName: String = "",
        @SerializedName("districtShortName") val districtShortName: String = "",
        @SerializedName("districtId") val districtId: Int = 0,
        @SerializedName("federalStateName") val federalStateName: String = "",
        @SerializedName("federalStateShortName") val federalStateShortName: String = "",
        @SerializedName("federalStateId") val federalStateId: Int = 0
    )

    companion object {
        private const val ASSET_NAME = "ppdd-ppa-administrative-unit-set.json"
        private const val TAG = "Districts"
    }
}
