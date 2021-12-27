package de.rki.coronawarnapp.coronatest.antigen.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private const val LEGACY_SHARED_PREFS_NAME = "ratprofile_localdata"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = LEGACY_SHARED_PREFS_NAME,
    produceMigrations = { context ->
        Timber.d("Migrating %s to DataStore", LEGACY_SHARED_PREFS_NAME)
        listOf(SharedPreferencesMigration(context, LEGACY_SHARED_PREFS_NAME))
    }
)

@Reusable
class RATProfileSettingsDataStore @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson,
    @AppScope private val appScope: CoroutineScope
) {

    private val dataStore = context.dataStore

    private val dataStoreFlow = context.dataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read RAT profile")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    val onboardedFlow: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[ONBOARDED_KEY] ?: false
    }

    val profileFlow: Flow<RATProfile?> = dataStoreFlow.map { preferences ->
        when (val rawProfile = preferences[PROFILE_KEY]) {
            null -> null
            else -> gson.fromJson<RATProfile>(rawProfile)
        }
    }

    fun setOnboarded() = appScope.launch {
        Timber.d("Set Onboarded to true")
        dataStore.edit { preferences ->
            preferences[ONBOARDED_KEY] = true
        }
    }

    fun updateProfile(profile: RATProfile) = appScope.launch {
        Timber.d("Updating RATProfile - new value: %s", profile)
        dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = gson.toJson(profile)
        }
    }

    fun deleteProfile() = appScope.launch {
        Timber.d("Deleting RATProfile")
        dataStore.edit { preferences ->
            preferences.remove(PROFILE_KEY)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val ONBOARDED_KEY = booleanPreferencesKey("ratprofile.settings.onboarded")
        private val PROFILE_KEY = stringPreferencesKey("ratprofile.settings.profile")

        private val TAG = tag<RATProfileSettingsDataStore>()
    }
}
