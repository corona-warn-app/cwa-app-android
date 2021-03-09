package de.rki.coronawarnapp.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.security.SecurityHelper.globalEncryptedSharedPreferencesInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * LocalData is responsible for all access to the shared preferences. Each preference is accessible
 * by a string which is stored in strings.xml.
 *
 * @see SharedPreferences
 */
object LocalData {

    private const val PREFERENCE_INTEROPERABILITY_IS_USED_AT_LEAST_ONCE =
        "preference_interoperability_is_used_at_least_once"

    private const val PREFERENCE_HAS_RISK_STATUS_LOWERED =
        "preference_has_risk_status_lowered"

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
    fun isTestResultAvailableNotificationSent(): Boolean {
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
    fun isTestResultAvailableNotificationSent(value: Boolean) =
        getSharedPreferenceInstance().edit(true) {
            putBoolean(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_test_result_notification),
                value
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
     * SUBMISSION DATA
     ****************************************************/

    private const val PREFERENCE_REGISTRATION_TOKEN = "preference_registration_token"

    /**
     * Gets the registration token that is needed for the submission process
     *
     * @return the registration token
     */
    fun registrationToken(): String? = getSharedPreferenceInstance()
        .getString(PREFERENCE_REGISTRATION_TOKEN, null)

    /**
     * Sets the registration token that is needed for the submission process
     *
     * @param value registration token as string
     */
    fun registrationToken(value: String?) {
        getSharedPreferenceInstance().edit(true) {
            putString(PREFERENCE_REGISTRATION_TOKEN, value)
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

    fun devicePairingSuccessfulTimestamp(): Long {
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

    private fun numberOfSuccessfulSubmissions(): Int {
        return getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_number_successful_submissions),
            0
        )
    }

    fun submissionWasSuccessful(): Boolean = numberOfSuccessfulSubmissions() >= 1

    fun isAllowedToSubmitDiagnosisKeys(isAllowedToSubmitDiagnosisKeys: Boolean) {
        getSharedPreferenceInstance().edit(true) {
            putBoolean(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_is_allowed_to_submit_diagnosis_keys),
                isAllowedToSubmitDiagnosisKeys
            )
        }
    }

    fun isAllowedToSubmitDiagnosisKeys(): Boolean {
        return getSharedPreferenceInstance().getBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_is_allowed_to_submit_diagnosis_keys),
            false
        )
    }

    /****************************************************
     * ENCRYPTED SHARED PREFERENCES HANDLING
     ****************************************************/

    private fun getSharedPreferenceInstance(): SharedPreferences = globalEncryptedSharedPreferencesInstance!!

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
        // If you make use of a FlowPreference, you need to manually clear it here
        Timber.w("LocalData.clear()")
    }
}
