package de.rki.coronawarnapp.submission

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Singleton

@Singleton
class SubmissionSettings @AssistedInject constructor(
    @Assisted val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("submission_localdata", Context.MODE_PRIVATE)
    }

    val hasGivenConsent = prefs.createFlowPreference(
        key = "key_submission_consent",
        defaultValue = false
    )

    @AssistedInject.Factory
    interface Factory : InjectedSubmissionSettingsFactory
}


interface InjectedSubmissionSettingsFactory {
    fun create(context: Context): SubmissionSettings
}

