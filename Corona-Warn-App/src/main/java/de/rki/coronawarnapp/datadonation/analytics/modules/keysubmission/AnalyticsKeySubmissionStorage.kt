package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import timber.log.Timber
import javax.inject.Inject

class AnalyticsPCRKeySubmissionStorage @Inject constructor(
    @AnalyticsKeySubmissionDataStore private val dataStore: DataStore<Preferences>,
) : AnalyticsKeySubmissionStorage(dataStore, sharedPrefKeySuffix) {
    companion object {
        const val sharedPrefKeySuffix = "" // the original
    }
}

class AnalyticsRAKeySubmissionStorage @Inject constructor(
    @AnalyticsKeySubmissionDataStore private val dataStore: DataStore<Preferences>,
) : AnalyticsKeySubmissionStorage(dataStore, sharedPrefKeySuffix) {
    companion object {
        const val sharedPrefKeySuffix = "_RAT"
    }
}

open class AnalyticsKeySubmissionStorage(
    private val dataStore: DataStore<Preferences>,
    private val sharedPrefKeySuffix: String
) {

    val testResultReceivedAt = dataStore.dataRecovering.distinctUntilChanged(
        key = longPreferencesKey("analytics_key_submission_testResultReceivedAt$sharedPrefKeySuffix"),
        defaultValue = -1L
    )

    suspend fun updateTestResultReceivedAt(value: Long) = dataStore.trySetValue(
        preferencesKey = longPreferencesKey("analytics_key_submission_testResultReceivedAt$sharedPrefKeySuffix"),
        value = value
    )

    val testRegisteredAt = dataStore.dataRecovering.distinctUntilChanged(
        key = longPreferencesKey("analytics_key_submission_testRegisteredAt$sharedPrefKeySuffix"),
        defaultValue = -1L
    )

    suspend fun updateTestRegisteredAt(value: Long) = dataStore.trySetValue(
        preferencesKey = longPreferencesKey("analytics_key_submission_testRegisteredAt$sharedPrefKeySuffix"),
        value = value
    )

    val submitted = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_submitted$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateSubmitted(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey("analytics_key_submission_submitted$sharedPrefKeySuffix"),
        value = value
    )

    val submittedAt = dataStore.dataRecovering.distinctUntilChanged(
        key = longPreferencesKey("analytics_key_submission_submittedAt$sharedPrefKeySuffix"),
        defaultValue = -1L
    )

    suspend fun updateSubmittedAt(value: Long) = dataStore.trySetValue(
        preferencesKey = longPreferencesKey("analytics_key_submission_submittedAt$sharedPrefKeySuffix"),
        value = value
    )

    val submittedInBackground = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_submittedInBackground$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateSubmittedInBackground(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey(
            "analytics_key_submission_submittedInBackground$sharedPrefKeySuffix"
        ),
        value = value
    )

    val submittedAfterCancel = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_submittedAfterCancel$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateSubmittedAfterCancel(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey(
            "analytics_key_submission_submittedAfterCancel$sharedPrefKeySuffix"
        ),
        value = value
    )

    val submittedAfterSymptomFlow = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_submittedAfterSymptomFlow$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateSubmittedAfterSymptomFlow(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey(
            "analytics_key_submission_submittedAfterSymptomFlow$sharedPrefKeySuffix"
        ),
        value = value
    )

    val lastSubmissionFlowScreen = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey("analytics_key_submission_lastSubmissionFlowScreen$sharedPrefKeySuffix"),
        defaultValue = Screen.UNKNOWN.code
    )

    suspend fun updateLastSubmissionFlowScreen(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(
            "analytics_key_submission_lastSubmissionFlowScreen$sharedPrefKeySuffix"
        ),
        value = value
    )

    val advancedConsentGiven = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_advancedConsentGiven$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateAdvancedConsentGiven(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey(
            "analytics_key_submission_advancedConsentGiven$sharedPrefKeySuffix"
        ),
        value = value
    )

    val registeredWithTeleTAN = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_registeredWithTeleTAN$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateRegisteredWithTeleTAN(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey(
            "analytics_key_submission_registeredWithTeleTAN$sharedPrefKeySuffix"
        ),
        value = value
    )

    val ewHoursSinceHighRiskWarningAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(
            "analytics_key_submission_hoursSinceHighRiskWarningAtTestRegistration$sharedPrefKeySuffix"
        ),
        defaultValue = -1
    )

    suspend fun updateEwHoursSinceHighRiskWarningAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(
            "analytics_key_submission_hoursSinceHighRiskWarningAtTestRegistration$sharedPrefKeySuffix"
        ),
        value = value
    )

    val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(
            "analytics_key_submission_daysSinceMostRecentDateAtRiskLevelAtTestRegistration$sharedPrefKeySuffix"
        ),
        defaultValue = -1
    )

    suspend fun updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(
            "analytics_key_submission_daysSinceMostRecentDateAtRiskLevelAtTestRegistration$sharedPrefKeySuffix"
        ),
        value = value
    )

    val ptHoursSinceHighRiskWarningAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(
            "analytics_key_submission_hoursSincePtHighRiskWarningAtTestRegistration$sharedPrefKeySuffix"
        ),
        defaultValue = -1
    )

    suspend fun updatePtHoursSinceHighRiskWarningAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(
            "analytics_key_submission_hoursSincePtHighRiskWarningAtTestRegistration$sharedPrefKeySuffix"
        ),
        value = value
    )

    val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration = dataStore.dataRecovering.distinctUntilChanged(
        key = intPreferencesKey(
            "analytics_key_submission_daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration$sharedPrefKeySuffix"
        ),
        defaultValue = -1
    )

    suspend fun updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(value: Int) = dataStore.trySetValue(
        preferencesKey = intPreferencesKey(
            "analytics_key_submission_daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration$sharedPrefKeySuffix"
        ),
        value = value
    )

    val submittedWithCheckIns = dataStore.dataRecovering.distinctUntilChanged(
        key = booleanPreferencesKey("analytics_key_submission_submittedWithCheckIns$sharedPrefKeySuffix"),
        defaultValue = false
    )

    suspend fun updateSubmittedWithCheckIns(value: Boolean) = dataStore.trySetValue(
        preferencesKey = booleanPreferencesKey(
            "analytics_key_submission_submittedWithCheckIns$sharedPrefKeySuffix"
        ),
        value = value
    )

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        dataStore.clear()
    }
}

private val TAG = tag<AnalyticsKeySubmissionStorage>()
