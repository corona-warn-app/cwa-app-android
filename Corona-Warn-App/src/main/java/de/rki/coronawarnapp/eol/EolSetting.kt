package de.rki.coronawarnapp.eol

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EolSetting @Inject constructor(
    @EolSettingsDataStore private val dataStore: DataStore<Preferences>
) {
    val eolDateTime = dataStore.data.map { prefs ->
        runCatching {
            ZonedDateTime.parse(prefs[EOL_DATE_TIME])
        }.getOrElse {
            ZonedDateTime.parse("2023-06-01T00:00:00+02:00")
        }
    }

    suspend fun setEolDateTime(dateTime: String) {
        dataStore.edit { prefs ->
            prefs[EOL_DATE_TIME] = dateTime
        }
    }

    companion object {
        private val EOL_DATE_TIME = stringPreferencesKey("EolSetting.eolDateTime")
    }
}
