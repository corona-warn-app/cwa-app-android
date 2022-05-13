package de.rki.coronawarnapp.profile.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.profile.legacy.RATProfile
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.reset.Resettable
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

@Reusable
class ProfileSettingsDataStore @Inject constructor(
    @ProfileDataStore private val dataStoreLazy: Lazy<DataStore<Preferences>>,
    @BaseGson private val gson: Gson,
    @AppScope private val appScope: CoroutineScope
) : Resettable {

    private val dataStore: DataStore<Preferences> get() = dataStoreLazy.get()
    private val dataStoreFlow = dataStore.data
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
        preferences[PROFILE_KEY]?.let { gson.fromJson(it) }
    }

    fun setOnboarded() = appScope.launch {
        Timber.d("Set Onboarded to true")
        dataStore.edit { preferences ->
            preferences[ONBOARDED_KEY] = true
        }
    }

    @Deprecated("legacy storage")
    fun updateProfile(profile: RATProfile) = appScope.launch {
        Timber.d("Updating RATProfile - new value: %s", profile)
        dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = gson.toJson(profile)
        }
    }

    @Deprecated("legacy storage")
    fun deleteProfile() = appScope.launch {
        Timber.d("Deleting RATProfile")
        dataStore.edit { preferences ->
            preferences.remove(PROFILE_KEY)
        }
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal val ONBOARDED_KEY = booleanPreferencesKey("ratprofile.settings.onboarded")

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal val PROFILE_KEY = stringPreferencesKey("ratprofile.settings.profile")

        private val TAG = tag<ProfileSettingsDataStore>()
    }
}
