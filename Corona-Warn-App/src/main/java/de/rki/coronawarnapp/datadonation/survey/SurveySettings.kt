package de.rki.coronawarnapp.datadonation.survey

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveySettings @Inject constructor(
    @SurveySettingsDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val objectMapper: ObjectMapper,
) : Resettable {

    val oneTimePassword = dataStore.dataRecovering.distinctUntilChanged(key = KEY_OTP).map { json ->
        try {
            if (json != null) {
                val otp = objectMapper.readValue<OneTimePassword>(json)
                requireNotNull(otp.uuid)
                requireNotNull(otp.time)
                otp
            } else {
                null
            }
        } catch (t: Throwable) {
            Timber.e(t, "failed to parse OneTimePassword from dataStore")
            null
        }
    }

    suspend fun updateOneTimePassword(otp: OneTimePassword?) = dataStore.edit {
        if (otp == null) {
            it.remove(KEY_OTP)
        } else {
            it[KEY_OTP] = objectMapper.writeValueAsString(otp)
        }
    }

    val otpAuthorizationResult: Flow<OTPAuthorizationResult?> = dataStore.dataRecovering.distinctUntilChanged(
        key = KEY_OTP_RESULT
    ).map { json ->
        try {
            if (json != null) {
                val result = objectMapper.readValue<OTPAuthorizationResult>(json)
                requireNotNull(result.uuid)
                requireNotNull(result.authorized)
                requireNotNull(result.redeemedAt)
                requireNotNull(result.invalidated)
                result
            } else {
                null
            }
        } catch (t: Throwable) {
            Timber.e(t, "failed to parse OTPAuthorizationResult from dataStore")
            null
        }
    }

    suspend fun updateOtpAuthorizationResult(result: OTPAuthorizationResult?) = dataStore.edit {
        if (result == null) {
            it.remove(KEY_OTP_RESULT)
        } else {
            it[KEY_OTP_RESULT] = objectMapper.writeValueAsString(result)
        }
    }

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    companion object {
        @VisibleForTesting
        val KEY_OTP = stringPreferencesKey("one_time_password")

        @VisibleForTesting
        val KEY_OTP_RESULT = stringPreferencesKey("otp_result")
    }
}
