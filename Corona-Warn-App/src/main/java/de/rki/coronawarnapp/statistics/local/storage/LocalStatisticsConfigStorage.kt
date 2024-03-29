package de.rki.coronawarnapp.statistics.local.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.datadonation.analytics.common.federalStateShortName
import de.rki.coronawarnapp.statistics.LocalStatisticsConfigDataStore
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsConfigStorage @Inject constructor(
    @LocalStatisticsConfigDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val objectMapper: ObjectMapper,
) : Resettable {

    val activeSelections = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_ACTIVE_SELECTIONS, defaultValue = ""
    ).map { value ->
        if (value.isEmpty()) {
            SelectedLocations()
        } else {
            objectMapper.readValue(value)
        }
    }

    suspend fun updateActiveSelections(locations: SelectedLocations) =
        dataStore.trySetValue(
            preferencesKey = PKEY_ACTIVE_SELECTIONS,
            value = objectMapper.writeValueAsString(locations)
        )

    val activePackages = activeSelections.map { selections ->
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
        updateActiveSelections(SelectedLocations())
    }

    companion object {
        val PKEY_ACTIVE_SELECTIONS = stringPreferencesKey("statistics.local.selections")
    }
}
