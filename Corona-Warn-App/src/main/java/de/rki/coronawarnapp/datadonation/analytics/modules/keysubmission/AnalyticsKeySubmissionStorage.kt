package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject

class AnalyticsKeySubmissionStorage @Inject constructor(
    @AppContext val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_key_submission_localdata", Context.MODE_PRIVATE)
    }

    val testResultReceivedAt = prefs.createFlowPreference(
        key = "analytics_key_submission_testResultReceivedAt",
        defaultValue = -1L
    )

    val testRegisteredAt = prefs.createFlowPreference(
        key = "analytics_key_submission_testRegisteredAt",
        defaultValue = -1L
    )

    val submitted = prefs.createFlowPreference(
        key = "analytics_key_submission_submitted",
        defaultValue = false
    )

    val submittedInBackground = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedInBackground",
        defaultValue = false
    )

    val submittedAfterCancel = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedAfterCancel",
        defaultValue = false
    )

    val submittedAfterSymptomFlow = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedAfterSymptomFlow",
        defaultValue = false
    )

    val lastSubmissionFlowScreen = prefs.createFlowPreference(
        key = "analytics_key_submission_lastSubmissionFlowScreen",
        defaultValue = SUBMISSION_FLOW_SCREEN_UNKNOWN
    )

    val advancedConsentGiven = prefs.createFlowPreference(
        key = "analytics_key_submission_advancedConsentGiven",
        defaultValue = false
    ) //set to true if the user agreed to share keys when scanning the QR code and the user has not withdrawn the consent; false otherwise.

    val registeredWithTeleTAN = prefs.createFlowPreference(
        key = "analytics_key_submission_registeredWithTeleTAN",
        defaultValue = false
    ) //is set to true of the user received a positive test result after entering a TeleTAN; otherwise set to false

    fun clear() {
        prefs.clearAndNotify()
    }
}
