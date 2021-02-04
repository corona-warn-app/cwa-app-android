package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveySettings @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val gson: Gson
) {

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
            } catch (t: Throwable) {
                Timber.e(t, "failed to parse OTP from preferences")
            }
            return null
        }
        set(value) =
            preferences
                .edit()
                .putString(KEY_OTP, if (value == null) null else gson.toJson(value))
                .apply()

    fun clear() = preferences.clearAndNotify()
}

private const val KEY_OTP = "one_time_password"
