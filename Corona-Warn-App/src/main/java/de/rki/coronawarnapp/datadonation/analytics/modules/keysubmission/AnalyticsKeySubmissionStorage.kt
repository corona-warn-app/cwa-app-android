package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import de.rki.coronawarnapp.server.protocols.internal.ppdd.TriStateBooleanOuterClass
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

    val ewRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_riskLevelAtTestRegistration",
        defaultValue = -1
    )

    val ptRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_ptRiskLevelAtTestRegistration",
        defaultValue = -1
    )

    val ewHoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSinceHighRiskWarningAtTestRegistration",
        defaultValue = -1
    )

    val ptHoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_ptHoursSinceHighRiskWarningAtTestRegistration",
        defaultValue = -1
    )

    val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_daysSinceMostRecentDateAtRiskLevelAtTestRegistration",
        defaultValue = -1
    )

    val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration",
        defaultValue = -1
    )

    val submittedWithCheckins = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedWithCheckins",
        defaultValue = TriStateBooleanOuterClass.TriStateBoolean.TSB_FALSE
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
