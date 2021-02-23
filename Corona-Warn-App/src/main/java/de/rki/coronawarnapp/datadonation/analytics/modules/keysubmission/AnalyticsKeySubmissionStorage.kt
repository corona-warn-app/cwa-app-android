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
        key = "analytics_key_submission_created_timestamp",
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
        defaultValue = 0
    )

    val advancedConsentGiven = prefs.createFlowPreference(
        key = "analytics_key_submission_advancedConsentGiven",
        defaultValue = false
    ) //set to true if the user agreed to share keys when scanning the QR code and the user has not withdrawn the consent; false otherwise.

    val hoursSinceTestResult = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSinceTestResult",
        defaultValue = 0
    )

    val hoursSinceTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSinceTestRegistration",
        defaultValue = 0
    )
    val daysSinceMostRecentDateAtRiskLevelAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_daysSinceMostRecentDateAtRiskLevelAtTestRegistration",
        defaultValue = 0
    ) //set to the difference in days between the Most Recent Date with High Risk or Most Recent Date with Low Risk
// (from the Risk Calculation) and the date of the test registration by ignoring any time information
// (i.e. the number that is displayed on the risk card at test registration). (same as for Test Result Metadata)

    val hoursSinceHighRiskWarningAtTestRegistration = prefs.createFlowPreference(
        key = "analytics_key_submission_hoursSinceHighRiskWarningAtTestRegistration",
        defaultValue = -1
    ) //set to the difference in hours between the timestamp of when the risk card turned red and the date of the test
    // registration. The value is rounded to an integer. If riskLevelAtTestRegistration is low, it is set to -1 (same as for Test Result Metadata)

    val submittedWithTeleTAN = prefs.createFlowPreference(
        key = "analytics_key_submission_submittedWithTeleTAN",
        defaultValue = false
    ) //is set to true of the user received a positive test result after entering a TeleTAN; otherwise set to false

    fun clear() {
        prefs.clearAndNotify()
    }
}
