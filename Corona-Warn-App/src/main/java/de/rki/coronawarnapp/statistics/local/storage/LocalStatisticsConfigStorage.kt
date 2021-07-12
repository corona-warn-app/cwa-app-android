package de.rki.coronawarnapp.statistics.local.storage

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsConfigStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson private val gson: Gson,
) {
    private val prefs by lazy {
        context.getSharedPreferences("statistics_local_config", Context.MODE_PRIVATE)
    }

    val activeDistricts = prefs.createFlowPreference(
        key = PKEY_ACTIVE_DISTRICTS,
        reader = FlowPreference.gsonReader(gson, emptySet<SelectedDistrict>()),
        writer = FlowPreference.gsonWriter(gson)
    )

    val activeStates = activeDistricts.flow
        .map {
            it.map { district -> district.district.federalStateShortName }
                .mapNotNull { stateId -> FederalStateToPackageId.getForName(stateId) }
                .distinct()
        }

    companion object {
        private const val PKEY_ACTIVE_DISTRICTS = "statistics.local.districts"
    }
}
