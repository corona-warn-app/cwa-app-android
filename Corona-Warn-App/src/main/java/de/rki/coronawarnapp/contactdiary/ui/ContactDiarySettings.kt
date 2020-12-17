package de.rki.coronawarnapp.submission

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiarySettings @Inject constructor(

    @AppContext val context: Context

) {


    private val prefs by lazy {
        context.getSharedPreferences("contact_diary_localdata", Context.MODE_PRIVATE)
    }

    val isOnboarded = prefs.createFlowPreference(
        key = "contact_diary_onboarded",
        defaultValue = false
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
