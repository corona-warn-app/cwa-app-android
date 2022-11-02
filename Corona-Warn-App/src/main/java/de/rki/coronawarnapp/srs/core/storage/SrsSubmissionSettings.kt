package de.rki.coronawarnapp.srs.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.srs.core.SrsSettingsDataStore
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import  com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

class SrsSubmissionSettings @Inject constructor(
    @SrsSettingsDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val mapper: ObjectMapper,
) {

    private val dataStoreFlow = dataStore.data
        .catch { e ->
            Timber.tag(TAG).e(e, "Failed to read Srs settings")
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }

    suspend fun getMostRecentSubmissionTime(): Instant {
        Timber.d("getMostRecentSubmissionTime()")
        return dataStoreFlow.map { prefs ->
            prefs[LAST_SUBMISSION_TIME_KEY]
        }.map { time ->
            time?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH
        }.first()
    }

    suspend fun setMostRecentSubmissionTime(executionTime: Instant = Instant.now()) {
        Timber.tag(TAG).d("setMostRecentSubmissionTime: %s", executionTime)
        runCatching {
            dataStore.edit { prefs ->
                prefs[LAST_SUBMISSION_TIME_KEY] = executionTime.toEpochMilli()
            }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "setMostRecentSubmissionTime failed")
        }
    }

    suspend fun getOtp(): SrsOtp? {
        Timber.d("getOtp()")
        return dataStoreFlow.map { prefs ->
            prefs[SRS_OTP_KEY]?.let {
                runCatching {
                    mapper.readValue<SrsOtp>(it)
                }.onFailure {
                    Timber.tag(TAG).e(it, "getOtp failed")
                }.getOrNull()
            }
        }.first()
    }

    suspend fun setOtp(srsOtp: SrsOtp) {
        Timber.tag(TAG).d("setOtp()")
        runCatching {
            dataStore.edit { prefs ->
                prefs[SRS_OTP_KEY] = mapper.writeValueAsString(srsOtp)
            }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "setOtp failed")
        }
    }

    companion object {
        internal val LAST_SUBMISSION_TIME_KEY = longPreferencesKey("srs.settings.lastSubmissionTime")
        internal val SRS_OTP_KEY = stringPreferencesKey("srs.settings.otp")
        val TAG = tag<SrsSubmissionSettings>()
    }
}
