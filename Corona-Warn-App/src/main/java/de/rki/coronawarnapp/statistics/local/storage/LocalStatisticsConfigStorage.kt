package de.rki.coronawarnapp.statistics.local.storage

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.analytics.common.federalStateShortName
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsConfigStorage @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val baseGson: Gson,
) : Resettable {
    private val prefs by lazy {
        context.getSharedPreferences("statistics_local_config", Context.MODE_PRIVATE)
    }

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

    val activeSelections = prefs.createFlowPreference(
        key = PKEY_ACTIVE_SELECTIONS,
        reader = FlowPreference.gsonReader(gson, SelectedLocations()),
        writer = FlowPreference.gsonWriter(gson)
    )

    val activePackages = activeSelections.flow
        .map { selections ->
            selections.locations.mapNotNull { selection ->
                when (selection) {
                    is SelectedStatisticsLocation.SelectedDistrict ->
                        FederalStateToPackageId.getForName(selection.district.federalStateShortName)
                    is SelectedStatisticsLocation.SelectedFederalState ->
                        FederalStateToPackageId.getForName(selection.federalState.federalStateShortName)
                }.also {
                    if (it == null) {
                        Timber.e("Failed to map Selected Location %s to package id", selection)
                    }
                }
            }.distinct()
        }

    override suspend fun reset() {
        Timber.d("reset()")
        activeSelections.update { SelectedLocations() }
    }

    companion object {
        private const val PKEY_ACTIVE_SELECTIONS = "statistics.local.selections"
    }
}
