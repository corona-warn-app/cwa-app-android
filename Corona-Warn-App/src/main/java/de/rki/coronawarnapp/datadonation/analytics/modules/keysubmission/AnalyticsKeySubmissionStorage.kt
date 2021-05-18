package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject

class AnalyticsPCRKeySubmissionStorage @Inject constructor(
    @AppContext context: Context
) : AnalyticsKeySubmissionStorage(context) {
    override val sharedPrefKey = "analytics_key_submission_localdata"
}

class AnalyticsRAKeySubmissionStorage @Inject constructor(
    @AppContext context: Context
) : AnalyticsKeySubmissionStorage(context) {
    override val sharedPrefKey = "analytics_ra_key_submission_localdata"
}

abstract class AnalyticsKeySubmissionStorage(
    val context: Context
) {
    abstract val sharedPrefKey: String

    private val prefs by lazy {
        context.getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE)
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

    val submittedAt = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedAt",
        defaultValue = -1L
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
        defaultValue = Screen.UNKNOWN.code
    )

    val advancedConsentGiven = prefs.createFlowPreference(
        key = "analytics_key_submission_advancedConsentGiven",
        defaultValue = false
    )

    val registeredWithTeleTAN = prefs.createFlowPreference(
        key = "analytics_key_submission_registeredWithTeleTAN",
        defaultValue = false
    )

    val riskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_riskLevelAtTestRegistration",
        defaultValue = -1
    )

    val hoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSinceHighRiskWarningAtTestRegistration",
        defaultValue = -1
    )

    val daysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_daysSinceMostRecentDateAtRiskLevelAtTestRegistration",
        defaultValue = -1
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
