package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveySettings @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val gson: Gson
) : Resettable {

    private val preferences by lazy {
        context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE)
    }

    var oneTimePassword: OneTimePassword?
        get() {
            try {
                val json = preferences.getString(KEY_OTP, null)
                if (json != null) {
                    val otp = gson.fromJson(json, OneTimePassword::class.java)
                    requireNotNull(otp.uuid)
                    requireNotNull(otp.time)
                    return otp
                }
                return null
            } catch (t: Throwable) {
                Timber.e(t, "failed to parse OTP from preferences")
                return null
            }
        }
        set(value) =
            preferences
                .edit()
                .putString(KEY_OTP, if (value == null) null else gson.toJson(value))
                .apply()

    var otpAuthorizationResult: OTPAuthorizationResult?
        get() {
            try {
                val json = preferences.getString(KEY_OTP_RESULT, null)
                if (json != null) {
                    val result = gson.fromJson(json, OTPAuthorizationResult::class.java)
                    requireNotNull(result.uuid)
                    requireNotNull(result.authorized)
                    requireNotNull(result.redeemedAt)
                    requireNotNull(result.invalidated)
                    return result
                }
                return null
            } catch (t: Throwable) {
                Timber.e(t, "failed to parse OTP from preferences")
                return null
            }
        }
        set(value) =
            preferences
                .edit()
                .putString(KEY_OTP_RESULT, if (value == null) null else gson.toJson(value))
                .apply()

    override suspend fun reset() {
        Timber.d("reset()")
        preferences.clearAndNotify()
    }
}

private const val KEY_OTP = "one_time_password"
private const val KEY_OTP_RESULT = "otp_result"
