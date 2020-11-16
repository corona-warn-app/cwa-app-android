package de.rki.coronawarnapp.submission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionSettings @Inject constructor(
    @AppContext private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("submission_localdata", Context.MODE_PRIVATE)
    }

    val hasGivenConsent = FlowPreference(
        preferences = prefs,
        key = "key_submission_consent",
        reader = FlowPreference.basicReader(false),
        writer = FlowPreference.basicWriter()
    )
}

