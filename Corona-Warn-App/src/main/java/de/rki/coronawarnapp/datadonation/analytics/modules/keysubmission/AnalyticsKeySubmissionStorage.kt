package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject

class AnalyticsPCRKeySubmissionStorage @Inject constructor(
    @AppContext context: Context
) : AnalyticsKeySubmissionStorage(context, "")

class AnalyticsRAKeySubmissionStorage @Inject constructor(
    @AppContext context: Context
) : AnalyticsKeySubmissionStorage(context, "_RAT")

abstract class AnalyticsKeySubmissionStorage(
    val context: Context,
    val sharedPrefKeySuffix: String
) {
    private val sharedPrefKey: String = "analytics_key_submission_localdata"

    private val prefs by lazy {
        context.getSharedPreferences(sharedPrefKey + sharedPrefKeySuffix, Context.MODE_PRIVATE)
    }

    val testResultReceivedAt = prefs.createFlowPreference(
        key = "analytics_key_submission_testResultReceivedAt" + sharedPrefKeySuffix,
        defaultValue = -1L
    )

    val testRegisteredAt = prefs.createFlowPreference(
        key = "analytics_key_submission_testRegisteredAt" + sharedPrefKeySuffix,
        defaultValue = -1L
    )

    val submitted = prefs.createFlowPreference(
        key = "analytics_key_submission_submitted" + sharedPrefKeySuffix,
        defaultValue = false
    )

    val submittedAt = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedAt" + sharedPrefKeySuffix,
        defaultValue = -1L
    )

    val submittedInBackground = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedInBackground" + sharedPrefKeySuffix,
        defaultValue = false
    )

    val submittedAfterCancel = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedAfterCancel" + sharedPrefKeySuffix,
        defaultValue = false
    )

    val submittedAfterSymptomFlow = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedAfterSymptomFlow" + sharedPrefKeySuffix,
        defaultValue = false
    )

    val lastSubmissionFlowScreen = prefs.createFlowPreference(
        key = "analytics_key_submission_lastSubmissionFlowScreen" + sharedPrefKeySuffix,
        defaultValue = Screen.UNKNOWN.code
    )

    val advancedConsentGiven = prefs.createFlowPreference(
        key = "analytics_key_submission_advancedConsentGiven" + sharedPrefKeySuffix,
        defaultValue = false
    )

    val registeredWithTeleTAN = prefs.createFlowPreference(
        key = "analytics_key_submission_registeredWithTeleTAN" + sharedPrefKeySuffix,
        defaultValue = false
    )

    val riskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_riskLevelAtTestRegistration" + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ewHoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSinceHighRiskWarningAtTestRegistration" + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_daysSinceMostRecentDateAtRiskLevelAtTestRegistration" + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ptHoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSincePtHighRiskWarningAtTestRegistration" + sharedPrefKeySuffix,
        defaultValue = -1
    )

    val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration$sharedPrefKeySuffix",
        defaultValue = -1
    )

    val submittedWithCheckIns = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedWithCheckIns$sharedPrefKeySuffix",
        defaultValue = false
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
