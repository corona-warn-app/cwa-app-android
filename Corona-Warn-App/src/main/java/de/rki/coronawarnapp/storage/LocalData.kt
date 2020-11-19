package de.rki.coronawarnapp.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.security.SecurityHelper.globalEncryptedSharedPreferencesInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * LocalData is responsible for all access to the shared preferences. Each preference is accessible
 * by a string which is stored in strings.xml.
 *
 * @see SharedPreferences
 */
object LocalData {

    private val TAG: String? = LocalData::class.simpleName

    private const val PREFERENCE_INTEROPERABILITY_IS_USED_AT_LEAST_ONCE =
        "preference_interoperability_is_used_at_least_once"

    private const val PREFERENCE_HAS_RISK_STATUS_LOWERED =
        "preference_has_risk_status_lowered"
    /****************************************************
     * ONBOARDING DATA
     ****************************************************/

    /**
     * Gets the boolean if the user has completed the onboarding
     * from the EncryptedSharedPrefs
     *
     * @return boolean if user is onboarded
     */
    fun isOnboarded(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_onboarding_completed),
        false
    )

    /**
     * Sets the boolean if the user has completed the onboarding
     * from the EncryptedSharedPrefs
     *
     * @param value boolean if onboarding was completed
     */
    fun isOnboarded(value: Boolean) = getSharedPreferenceInstance().edit(true) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_onboarding_completed), value
        )
    }

    /**
     * Gets the time when the user has completed the onboarding
     * from the EncryptedSharedPrefs
     *
     * @return
     */
    fun onboardingCompletedTimestamp(): Long? {
        val timestamp = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_onboarding_completed_timestamp), 0L
        )

        if (timestamp == 0L) return null
        return timestamp
    }

    /**
     * Sets the time when the user has completed the onboarding
     * from the EncryptedSharedPrefs
     * @param value
     */
    fun onboardingCompletedTimestamp(value: Long) = getSharedPreferenceInstance().edit(true) {
        putLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_onboarding_completed_timestamp), value
        )
    }

    /**
     * Gets the boolean if the user has received the background warning
     * from the EncryptedSharedPrefs
     *
     * @return boolean if background warning was shown
     */
    fun isBackgroundCheckDone(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_background_check_done),
        false
    )

    /**
     * Sets the boolean if the user has received the background warning
     * from the EncryptedSharedPrefs
     *
     * @param value boolean if background warning was shown
     */
    fun isBackgroundCheckDone(value: Boolean) = getSharedPreferenceInstance().edit(true) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_background_check_done), value
        )
    }
    /****************************************************
     * TRACING DATA
     ****************************************************/

    /**
     * Gets the initial timestamp when tracing was activated for the first time in ms
     *
     * @return timestamp in ms
     */
    fun initialTracingActivationTimestamp(): Long? {
        val timestamp = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_initial_tracing_activation_time),
            0L
        )

        if (timestamp == 0L) return null
        return timestamp
    }

    /**
     * Sets the initial timestamp when tracing was activated for the first time in ms
     *
     * @param value timestamp in ms
     */
    fun initialTracingActivationTimestamp(value: Long) =
        getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_initial_tracing_activation_time),
                value
            )
        }

    /**
     * Gets the timestamp when the user stopped Exposure Notification tracing the last time
     * from the EncryptedSharedPrefs
     *
     * @return timestamp in ms
     */
    fun lastNonActiveTracingTimestamp(): Long? {
        val timestamp = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_last_non_active_tracing_timestamp),
            0L
        )
        if (timestamp == 0L) return null
        return timestamp
    }

    /**
     * Sets the timestamp when the user stopped Exposure Notification tracing the last time
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp in ms
     */
    fun lastNonActiveTracingTimestamp(value: Long?) = getSharedPreferenceInstance().edit(true) {
        putLong(
            CoronaWarnApplication.getAppContext().getString(
                R.string.preference_last_non_active_tracing_timestamp
            ),
            value ?: 0L
        )
    }

    /**
     * Sets the total amount of time the tracing was not active
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp in ms
     */
    fun totalNonActiveTracing(value: Long?) {
        getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_total_non_active_tracing),
                value ?: 0L
            )
        }
    }

    /**
     * Gets the total amount of time the tracing was not active
     * from the EncryptedSharedPrefs
     *
     * @return timestamp in ms
     */
    fun totalNonActiveTracing(): Long {
        return getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_total_non_active_tracing),
            0L
        )
    }

    /**

     * Gets the timestamp when the Background Polling Began initially
     * from the EncryptedSharedPrefs
     *
     * @return timestamp in ms
     */
    fun initialPollingForTestResultTimeStamp(): Long {
        return getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_polling_test_result_started),
            0L
        )
    }

    /**
     * Sets the timestamp when the Background Polling Began initially
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp in ms
     */
    fun initialPollingForTestResultTimeStamp(value: Long) =
        getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_polling_test_result_started),
                value
            )
        }

    /**

     * Gets the flag if notification is executed on Status Change
     * from the EncryptedSharedPrefs
     *
     * @return boolean
     */
    fun isTestResultNotificationSent(): Boolean {
        return getSharedPreferenceInstance().getBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_test_result_notification),
            false
        )
    }

    /**
     * Sets the flag if notification is executed on Status Change
     * from the EncryptedSharedPrefs
     *
     * @param value boolean
     */
    fun isTestResultNotificationSent(value: Boolean) =
        getSharedPreferenceInstance().edit(true) {
            putBoolean(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_test_result_notification),
                value
            )
        }

    /****************************************************
     * RISK LEVEL
     ****************************************************/

    /**
     * Gets the last calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @return
     */
    fun lastCalculatedRiskLevel(): RiskLevel {
        val rawRiskLevel = getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_risk_level_score),
            RiskLevel.UNDETERMINED.raw
        )
        return RiskLevel.forValue(rawRiskLevel)
    }

    /**
     * Sets the last calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @param rawRiskLevel
     */
    fun lastCalculatedRiskLevel(rawRiskLevel: Int) =
        getSharedPreferenceInstance().edit(true) {
            putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_risk_level_score),
                rawRiskLevel
            )
        }

    /**
     * Gets the last successfully calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @return
     */
    fun lastSuccessfullyCalculatedRiskLevel(): RiskLevel {
        val rawRiskLevel = getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_risk_level_score_successful),
            RiskLevel.UNDETERMINED.raw
        )
        return RiskLevel.forValue(rawRiskLevel)
    }

    /**
     * Sets the last calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @param rawRiskLevel
     */
    fun lastSuccessfullyCalculatedRiskLevel(rawRiskLevel: Int) =
        getSharedPreferenceInstance().edit(true) {
            putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_risk_level_score_successful),
                rawRiskLevel
            )
        }

    /**
     * Gets the boolean if the user has seen the explanation dialog for the
     * risk level tracing days
     * from the EncryptedSharedPrefs
     *
     * @return boolean if user is onboarded
     */
    fun tracingExplanationDialogWasShown(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_risk_days_explanation_shown),
        false
    )

    /**
     * Sets the boolean if the user has seen the explanation dialog for the
     * risk level tracing days
     * from the EncryptedSharedPrefs
     *
     * @param value boolean if onboarding was completed
     */
    fun tracingExplanationDialogWasShown(value: Boolean) =
        getSharedPreferenceInstance().edit(true) {
            putBoolean(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_risk_days_explanation_shown), value
            )
        }

    /**
     * Sets a boolean depending whether the risk level decreased or not.
     */
    private val isUserToBeNotifiedOfLoweredRiskLevelFlowInternal by lazy {
        MutableStateFlow(isUserToBeNotifiedOfLoweredRiskLevel)
    }
    val isUserToBeNotifiedOfLoweredRiskLevelFlow: Flow<Boolean> by lazy {
        isUserToBeNotifiedOfLoweredRiskLevelFlowInternal
    }
    var isUserToBeNotifiedOfLoweredRiskLevel: Boolean
        get() = getSharedPreferenceInstance().getBoolean(
            PREFERENCE_HAS_RISK_STATUS_LOWERED,
            false
        )
        set(value) = getSharedPreferenceInstance()
            .edit(commit = true) { putBoolean(PREFERENCE_HAS_RISK_STATUS_LOWERED, value) }
            .also { isUserToBeNotifiedOfLoweredRiskLevelFlowInternal.value = value }

    /****************************************************
     * SERVER FETCH DATA
     ****************************************************/

    private val dateMapperForFetchTime: (Long) -> Date? = {
        if (it != 0L) Date(it) else null
    }

    private val lastTimeDiagnosisKeysFetchedFlowPref by lazy {
        getSharedPreferenceInstance()
            .createFlowPreference<Long>(key = "preference_timestamp_diagnosis_keys_fetch", 0L)
    }

    fun lastTimeDiagnosisKeysFromServerFetchFlow() = lastTimeDiagnosisKeysFetchedFlowPref.flow
        .map { dateMapperForFetchTime(it) }

    fun lastTimeDiagnosisKeysFromServerFetch() =
        dateMapperForFetchTime(lastTimeDiagnosisKeysFetchedFlowPref.value)

    fun lastTimeDiagnosisKeysFromServerFetch(value: Date?) =
        lastTimeDiagnosisKeysFetchedFlowPref.update { value?.time ?: 0L }

    /**
     * Gets the last time of successful risk level calculation as long
     * from the EncryptedSharedPrefs
     *
     * @return Long
     */
    fun lastTimeRiskLevelCalculation(): Long? {
        val time = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_timestamp_risk_level_calculation),
            0L
        )
        return Date(time).time
    }

    /**
     * Sets the last time of successful risk level calculation as long
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp as Long
     */
    fun lastTimeRiskLevelCalculation(value: Long?) {
        getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_timestamp_risk_level_calculation),
                value ?: 0L
            )
        }
    }

    /****************************************************
     * EXPOSURE NOTIFICATION DATA
     ****************************************************/

    /**
     * Gets the last token that was used to provide the diagnosis keys to the Exposure Notification API
     *
     * @return UUID as string
     */
    fun googleApiToken(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_string_google_api_token),
        null
    )

    /**
     * Sets the last token that was used to provide the diagnosis keys to the Exposure Notification API
     *
     * @param value UUID as string
     */
    fun googleApiToken(value: String?) = getSharedPreferenceInstance().edit(true) {
        putString(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_string_google_api_token),
            value
        )
    }

    /****************************************************
     * SETTINGS DATA
     ****************************************************/

    private const val PKEY_NOTIFICATIONS_RISK_ENABLED = "preference_notifications_risk_enabled"

    private val isNotificationsRiskEnabledFlowInternal by lazy {
        MutableStateFlow(isNotificationsRiskEnabled)
    }
    val isNotificationsRiskEnabledFlow: Flow<Boolean> by lazy {
        isNotificationsRiskEnabledFlowInternal
    }
    var isNotificationsRiskEnabled: Boolean
        get() = getSharedPreferenceInstance().getBoolean(PKEY_NOTIFICATIONS_RISK_ENABLED, true)
        set(value) = getSharedPreferenceInstance().edit(true) {
            putBoolean(PKEY_NOTIFICATIONS_RISK_ENABLED, value)
            isNotificationsRiskEnabledFlowInternal.value = value
        }

    private const val PKEY_NOTIFICATIONS_TEST_ENABLED = "preference_notifications_test_enabled"
    private val isNotificationsTestEnabledFlowInternal by lazy {
        MutableStateFlow(isNotificationsTestEnabled)
    }
    val isNotificationsTestEnabledFlow: Flow<Boolean> by lazy {
        isNotificationsTestEnabledFlowInternal
    }
    var isNotificationsTestEnabled: Boolean
        get() = getSharedPreferenceInstance().getBoolean(PKEY_NOTIFICATIONS_TEST_ENABLED, true)
        set(value) = getSharedPreferenceInstance().edit(true) {
            putBoolean(PKEY_NOTIFICATIONS_TEST_ENABLED, value)
            isNotificationsTestEnabledFlowInternal.value = value
        }

    private const val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT = "preference_positive_test_result_reminder_count"
    var numberOfRemainingPositiveTestResultReminders: Int
        get() = getSharedPreferenceInstance().getInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT, Int.MIN_VALUE)
        set(value) = getSharedPreferenceInstance().edit(true) {
            putInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT, value)
        }

    /**
     * Gets the decision if background jobs are enabled
     *
     * @return
     */
    fun isBackgroundJobEnabled(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_background_job_allowed),
        false
    )

    /**
     * Toggles the decision if background jobs are enabled
     *
     */
    fun toggleBackgroundJobEnabled() = getSharedPreferenceInstance().edit(true) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_background_job_allowed),
            !isBackgroundJobEnabled()
        )
    }

    /**
     * Gets the boolean if the user has mobile data enabled
     *
     * @return
     */
    fun isMobileDataEnabled(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_mobile_data_allowed),
        false
    )

    /**
     * Toggles the boolean if the user has mobile data enabled
     *
     */
    fun toggleMobileDataEnabled() = getSharedPreferenceInstance().edit(true) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_mobile_data_allowed),
            !isMobileDataEnabled()
        )
    }

    /****************************************************
     * SUBMISSION DATA
     ****************************************************/

    /**
     * Gets the registration token that is needed for the submission process
     *
     * @return the registration token
     */
    fun registrationToken(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_registration_token),
        null
    )

    /**
     * Sets the registration token that is needed for the submission process
     *
     * @param value registration token as string
     */
    fun registrationToken(value: String?) {
        getSharedPreferenceInstance().edit(true) {
            putString(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_registration_token),
                value
            )
        }
    }

    fun initialTestResultReceivedTimestamp(value: Long) =
        getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_initial_result_received_time),
                value
            )
        }

    fun initialTestResultReceivedTimestamp(): Long? {
        val timestamp = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_initial_result_received_time),
            0L
        )

        if (timestamp == 0L) return null
        return timestamp
    }

    fun devicePairingSuccessfulTimestamp(value: Long) =
        getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_device_pairing_successful_time),
                value
            )
        }

    fun devicePairingSuccessfulTimestamp(): Long? {
        return getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_device_pairing_successful_time),
            0L
        )
    }

    fun numberOfSuccessfulSubmissions(value: Int) =
        getSharedPreferenceInstance().edit(true) {
            putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_number_successful_submissions),
                value
            )
        }

    fun numberOfSuccessfulSubmissions(): Int {
        return getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_number_successful_submissions),
            0
        )
    }

    fun submissionWasSuccessful(): Boolean = numberOfSuccessfulSubmissions() >= 1

    fun testGUID(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_test_guid),
        null
    )

    fun testGUID(value: String?) {
        getSharedPreferenceInstance().edit(true) {
            putString(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_test_guid),
                value
            )
        }
    }

    fun authCode(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_auth_code),
        null
    )

    fun authCode(value: String?) {
        getSharedPreferenceInstance().edit(true) {
            putString(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_auth_code),
                value
            )
        }
    }

    fun isAllowedToSubmitDiagnosisKeys(isAllowedToSubmitDiagnosisKeys: Boolean) {
        getSharedPreferenceInstance().edit(true) {
            putBoolean(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_is_allowed_to_submit_diagnosis_keys),
                isAllowedToSubmitDiagnosisKeys
            )
        }
    }

    fun isAllowedToSubmitDiagnosisKeys(): Boolean? {
        return getSharedPreferenceInstance().getBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_is_allowed_to_submit_diagnosis_keys),
            false
        )
    }

    fun teletan(value: String?) = getSharedPreferenceInstance().edit(true) {
        putString(
            CoronaWarnApplication.getAppContext().getString(R.string.preference_teletan),
            value
        )
    }

    fun teletan(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_teletan), null
    )

    fun backgroundNotification(value: Boolean) = getSharedPreferenceInstance().edit(true) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_background_notification),
            value
        )
    }

    fun backgroundNotification(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_background_notification), false
    )

    /****************************************************
     * ENCRYPTED SHARED PREFERENCES HANDLING
     ****************************************************/

    fun getSharedPreferenceInstance(): SharedPreferences = globalEncryptedSharedPreferencesInstance

    /****************************************************
     * INTEROPERABILITY
     ****************************************************/

    var isInteroperabilityShownAtLeastOnce: Boolean
        get() {
            return getSharedPreferenceInstance().getBoolean(
                PREFERENCE_INTEROPERABILITY_IS_USED_AT_LEAST_ONCE,
                false
            )
        }
        set(value) {
            getSharedPreferenceInstance().edit(true) {
                putBoolean(PREFERENCE_INTEROPERABILITY_IS_USED_AT_LEAST_ONCE, value)
            }
        }

    fun clear() {
        lastTimeDiagnosisKeysFetchedFlowPref.update { 0L }
    }
}
