package de.rki.coronawarnapp.eol

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    val isLoggerAllowed = dataStore.data.map { prefs ->
        runCatching { prefs[EOL_LOGGER_FLAG] ?: false }.getOrElse { false }
    }

    suspend fun setLoggerAllowed(flag: Boolean) {
        dataStore.edit { prefs ->
            prefs[EOL_LOGGER_FLAG] = flag
        }
    }

    companion object {
        private val EOL_DATE_TIME = stringPreferencesKey("EolSetting.eolDateTime")
        private val EOL_LOGGER_FLAG = booleanPreferencesKey("EolSetting.eolLoggerAllowed")
    }
}
