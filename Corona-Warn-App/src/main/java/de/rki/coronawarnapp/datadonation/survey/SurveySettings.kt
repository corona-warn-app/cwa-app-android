package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
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

    val oneTimePassword =
        preferences.createFlowPreference(
            key = "one_time_password",
            reader = FlowPreference.gsonReader<OneTimePassword?>(gson, null),
            writer = FlowPreference.gsonWriter(gson)
        )

    fun clear() = preferences.clearAndNotify()
}
