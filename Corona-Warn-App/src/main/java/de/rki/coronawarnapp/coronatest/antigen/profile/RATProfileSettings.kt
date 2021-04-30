package de.rki.coronawarnapp.coronatest.antigen.profile

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import javax.inject.Inject

@Reusable
class RATProfileSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {

    private val prefs by lazy {
        context.getSharedPreferences("ratprofile_localdata", Context.MODE_PRIVATE)
    }

    val profile = prefs.createFlowPreference(
        key = PREFS_KEY_PROFILE,
        reader = { key ->
            val rawProfile = getString(key, null)
            if (rawProfile == null) {
                null
            } else {
                gson.fromJson<RATProfile>(rawProfile)
            }
        },
        writer = { key, value ->
            putString(key, gson.toJson(value))
        }
    )

    val onboarded = prefs.createFlowPreference(
        key = PREFS_KEY_ONBOARDED,
        defaultValue = false
    )

    fun deleteProfile() = prefs.edit(commit = true) {
        remove(PREFS_KEY_PROFILE)
    }

    companion object {
        private const val PREFS_KEY_PROFILE = "ratprofile.settings.profile"
        private const val PREFS_KEY_ONBOARDED = "ratprofile.settings.onboarded"
    }
}
