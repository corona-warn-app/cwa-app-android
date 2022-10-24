package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsExposureWindow
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AnalyticsExposureWindowsSettings @Inject constructor(
    @BaseJackson private val objectMapper: ObjectMapper,
    @AnalyticsExposureWindowsDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    val currentExposureWindows = dataStore.dataRecovering.distinctUntilChanged(
        key = PREFS_KEY_CURRENT_EXPOSURE_WINDOWS, defaultValue = ""
    ).map { value ->
        if (value.isNotEmpty()) {
            objectMapper.readValue(value) as List<AnalyticsExposureWindow>
        } else null
    }

    suspend fun updateCurrentExposureWindows(value: List<AnalyticsExposureWindow>) = dataStore.trySetValue(
        preferencesKey = PREFS_KEY_CURRENT_EXPOSURE_WINDOWS, value = objectMapper.writeValueAsString(value)
    )

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }
}

private val PREFS_KEY_CURRENT_EXPOSURE_WINDOWS = stringPreferencesKey("analytics_currentExposureWindows")
