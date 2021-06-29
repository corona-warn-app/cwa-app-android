package de.rki.coronawarnapp.statistics.local.storage

import android.content.Context
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsConfigStorage @Inject constructor(
    @AppContext val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("statistics_local_config", Context.MODE_PRIVATE)
    }

    val activeStates = prefs.createFlowPreference(PKEY_ACTIVE_STATES, emptyList<FederalStateToPackageId>())

    companion object {
        private const val PKEY_ACTIVE_STATES = "statistics.local.states"
    }
}
