package de.rki.coronawarnapp.storage

import android.content.SharedPreferences
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.security.SecurityHelper.globalEncryptedSharedPreferencesInstance
import java.util.Date

/**
 * LocalData is responsible for all access to the shared preferences. Each preference is accessible
 * by a string which is stored in strings.xml.
 *
 * @see SharedPreferences
 */
object LocalData {

    private val TAG: String? = LocalData::class.simpleName

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
    fun isOnboarded(value: Boolean) = with(getSharedPreferenceInstance().edit()) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_onboarding_completed), value
        )
        commit()
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

        // TODO need this for nullable ref, shout not be goto for nullable storage
        if (timestamp == 0L) return null

        return timestamp
    }

    /**
     * Sets the initial timestamp when tracing was activated for the first time in ms
     *
     * @param value timestamp in ms
     */
    fun initialTracingActivationTimestamp(value: Long) =
        with(getSharedPreferenceInstance().edit()) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_initial_tracing_activation_time),
                value
            )
            commit()
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
        // TODO need this for nullable ref, shout not be goto for nullable storage
        if (timestamp == 0L) return null

        return timestamp
    }

    /**
     * Sets the timestamp when the user stopped Exposure Notification tracing the last time
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp in ms
     */
    fun lastNonActiveTracingTimestamp(value: Long?) = with(getSharedPreferenceInstance().edit()) {
        // TODO need this for nullable ref, shout not be goto for nullable storage
        putLong(
            CoronaWarnApplication.getAppContext().getString(
                R.string.preference_last_non_active_tracing_timestamp
            ),
            value ?: 0L
        )
        commit()
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
     * Sets the total amount of time the tracing was not active
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp in ms
     */
    fun totalNonActiveTracing(value: Long?) {
        // TODO need this for nullable ref, shout not be goto for nullable storage
        with(getSharedPreferenceInstance().edit()) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_total_non_active_tracing),
                value ?: 0L
            )
            commit()
        }
    }

    /****************************************************
     * SERVER FETCH DATA
     ****************************************************/

    /**
     * Gets the last time the server fetched the diagnosis keys from the server as Date object
     * from the EncryptedSharedPrefs
     *
     * @return timestamp as Date
     */
    // TODO should be changed to Long as well to align with other timestamps
    fun lastTimeDiagnosisKeysFromServerFetch(): Date? {
        val time = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_m_timestamp_diagnosis_keys_fetch),
            0L
        )
        // TODO need this for nullable ref, shout not be goto for nullable storage
        if (time == 0L) return null

        return Date(time)
    }

    /**
     * Sets the last time the server fetched the diagnosis keys from the server as Date object
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp as Date
     */
    fun lastTimeDiagnosisKeysFromServerFetch(value: Date?) {
        // TODO need this for nullable ref, shout not be goto for nullable storage
        with(getSharedPreferenceInstance().edit()) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_m_timestamp_diagnosis_keys_fetch),
                value?.time ?: 0L
            )
            commit()
        }
    }

    /**
     * Gets the last timestamp the user manually triggered the key retrieval process
     *
     * @return Long
     */
    fun lastTimeManualDiagnosisKeysRetrieved(): Long = getSharedPreferenceInstance().getLong(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_m_timestamp_manual_diagnosis_keys_retrieval),
        0L
    )

    /**
     * Sets the last timestamp the user manually triggered the key retrieval process
     */
    fun lastTimeManualDiagnosisKeysRetrieved(value: Long) =
        with(getSharedPreferenceInstance().edit()) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_m_timestamp_manual_diagnosis_keys_retrieval),
                value
            )
            commit()
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
            .getString(R.string.preference_m_string_google_api_token),
        null
    )

    /**
     * Sets the last token that was used to provide the diagnosis keys to the Exposure Notification API
     *
     * @param value UUID as string
     */
    fun googleApiToken(value: String?) = with(getSharedPreferenceInstance().edit()) {
        putString(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_m_string_google_api_token),
            value
        )
        commit()
    }

    /****************************************************
     * SETTINGS DATA
     ****************************************************/

    /**
     * Gets the user decision if notification should be enabled for a risk change
     *
     * @return
     */
    fun isNotificationsRiskEnabled(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_notifications_risk_enabled),
        true
    )

    /**
     * Toggles the user decision if notification should be enabled for a risk change
     *
     */
    fun toggleNotificationsRiskEnabled() = with(getSharedPreferenceInstance().edit()) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_notifications_risk_enabled),
            !isNotificationsRiskEnabled()
        )
        commit()
    }

    fun isNotificationsTestEnabled(): Boolean = getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_notifications_test_enabled),
        true
    )

    fun toggleNotificationsTestEnabled() = with(getSharedPreferenceInstance().edit()) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_notifications_test_enabled),
            !isNotificationsTestEnabled()
        )
        commit()
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
    fun toggleBackgroundJobEnabled() = with(getSharedPreferenceInstance().edit()) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_background_job_allowed),
            !isBackgroundJobEnabled()
        )
        commit()
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
    fun toggleMobileDataEnabled() = with(getSharedPreferenceInstance().edit()) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_mobile_data_allowed),
            !isMobileDataEnabled()
        )
        commit()
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
            .getString(R.string.preference_m_registration_token),
        null
    )

    /**
     * Sets the registration token that is needed for the submission process
     *
     * @param value registration token as string
     */
    fun registrationToken(value: String?) {
        with(getSharedPreferenceInstance().edit()) {
            putString(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_m_registration_token),
                value
            )
            commit()
        }
    }

    fun inititalTestResultReceivedTimestamp(value: Long) =
        with(getSharedPreferenceInstance().edit()) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_initial_result_received_time),
                value
            )
            commit()
        }

    fun inititalTestResultReceivedTimestamp(): Long? {
        val timestamp = getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_initial_result_received_time),
            0L
        )

        // TODO need this for nullable ref, shout not be goto for nullable storage
        if (timestamp == 0L) return null

        return timestamp
    }

    fun numberOfSuccessfulSubmissions(value: Int) =
        with(getSharedPreferenceInstance().edit()) {
            putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_number_successful_submissions),
                value
            )
            commit()
        }

    fun numberOfSuccessfulSubmissions(): Int {
        return getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_number_successful_submissions),
            0
        )
    }

    fun testGUID(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_m_test_guid),
        null
    )

    fun testGUID(value: String?) {
        with(getSharedPreferenceInstance().edit()) {
            putString(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_m_test_guid),
                value
            )
            commit()
        }
    }

    fun authCode(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_m_auth_code),
        null
    )

    fun authCode(value: String?) {
        with(getSharedPreferenceInstance().edit()) {
            putString(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_m_auth_code),
                value
            )
            commit()
        }
    }

    fun isAllowedToSubmitDiagnosisKeys(isAllowedToSubmitDiagnosisKeys: Boolean) {
        with(getSharedPreferenceInstance().edit()) {
            putBoolean(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_m_is_allowed_to_submit_diagnosis_keys),
                isAllowedToSubmitDiagnosisKeys
            )
            commit()
        }
    }

    fun isAllowedToSubmitDiagnosisKeys(): Boolean? {
        return getSharedPreferenceInstance().getBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_m_is_allowed_to_submit_diagnosis_keys),
            false
        )
    }

    fun teletan(value: String?) = with(getSharedPreferenceInstance().edit()) {
        putString(
            CoronaWarnApplication.getAppContext().getString(R.string.preference_teletan),
            value
        )
        commit()
    }

    fun teletan(): String? = getSharedPreferenceInstance().getString(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_teletan), null
    )

    /****************************************************
     * ENCRYPTED SHARED PREFERENCES HANDLING
     ****************************************************/

    fun getSharedPreferenceInstance(): SharedPreferences = globalEncryptedSharedPreferencesInstance

    fun getBackgroundWorkRelatedPreferences() = listOf(
        CoronaWarnApplication.getAppContext().getString(R.string.preference_background_job_allowed),
        CoronaWarnApplication.getAppContext().getString(R.string.preference_mobile_data_allowed)
    )

    fun getLastFetchDatePreference() =
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_m_timestamp_diagnosis_keys_fetch)
}
