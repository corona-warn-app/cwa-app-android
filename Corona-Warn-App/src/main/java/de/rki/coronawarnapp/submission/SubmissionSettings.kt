package de.rki.coronawarnapp.submission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionSettings @Inject constructor(
    @AppContext val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("submission_localdata", Context.MODE_PRIVATE)
    }

    val hasGivenConsent = prefs.createFlowPreference(
        key = "key_submission_consent",
        defaultValue = false
    )
}
