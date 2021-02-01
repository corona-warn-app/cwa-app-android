package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveySettings @Inject constructor(
    @AppContext val context: Context
) {

    private val preferences by lazy {
        context.getSharedPreferences("survey_localdata", Context.MODE_PRIVATE)
    }

    val oneTimePassword =
        preferences.createFlowPreference<OneTimePassword?>(
            key = "one_time_password",
            defaultValue = null
        )

    fun clear() = preferences.clearAndNotify()
}
