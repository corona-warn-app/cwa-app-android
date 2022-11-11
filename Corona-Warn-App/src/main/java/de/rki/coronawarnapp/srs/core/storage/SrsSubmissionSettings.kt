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
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.flow.Flow
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

    val otp: Flow<SrsOtp?> = dataStoreFlow.map { prefs ->
        prefs[SRS_OTP_KEY]?.let {
            runCatching {
                mapper.readValue<SrsOtp>(it)
            }.onFailure {
                Timber.tag(TAG).e(it, "Failed to read")
            }.getOrNull()
        }
    }

    val mostRecentSubmissionTime = dataStoreFlow.map { prefs ->
        prefs[LAST_SUBMISSION_TIME_KEY]?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH
    }

    suspend fun getMostRecentSubmissionTime(): Instant {
        Timber.d("getMostRecentSubmissionTime()")
        return mostRecentSubmissionTime.first()
    }

    suspend fun setMostRecentSubmissionTime(time: Instant = Instant.now()) {
        Timber.tag(TAG).d("setMostRecentSubmissionTime: %s", time)
        runCatching {
            dataStore.edit { prefs ->
                prefs[LAST_SUBMISSION_TIME_KEY] = time.toEpochMilli()
            }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "setMostRecentSubmissionTime failed")
        }
    }

    suspend fun getOtp(): SrsOtp? {
        Timber.d("getOtp()")
        return otp.first()
    }

    suspend fun setOtp(srsOtp: SrsOtp) {
        Timber.tag(TAG).d("save otp()")
        runCatching {
            dataStore.edit { prefs ->
                prefs[SRS_OTP_KEY] = mapper.writeValueAsString(srsOtp)
            }
        }.onFailure { e ->
            Timber.tag(TAG).e(e, "setOtp failed")
        }
    }

    suspend fun resetMostRecentSubmission() {
        Timber.tag(TAG).d("resetMostRecentSubmission()")
        dataStore.edit { prefs ->
            prefs.remove(LAST_SUBMISSION_TIME_KEY)
        }
    }

    suspend fun resetOtp() {
        Timber.tag(TAG).d("resetOtp()")
        dataStore.edit { prefs ->
            prefs.remove(SRS_OTP_KEY)
        }
    }

    companion object {
        internal val LAST_SUBMISSION_TIME_KEY = longPreferencesKey("srs.settings.lastSubmissionTime")
        internal val SRS_OTP_KEY = stringPreferencesKey("srs.settings.otp")
        val TAG = tag<SrsSubmissionSettings>()
    }
}
