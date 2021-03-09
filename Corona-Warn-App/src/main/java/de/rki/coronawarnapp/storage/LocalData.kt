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
                .getString(R.string.preference_onboarding_completed),
            value
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
                .getString(R.string.preference_onboarding_completed_timestamp),
            0L
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
                .getString(R.string.preference_onboarding_completed_timestamp),
            value
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
                .getString(R.string.preference_background_check_done),
            value
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
