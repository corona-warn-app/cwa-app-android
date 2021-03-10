package de.rki.coronawarnapp.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.security.SecurityHelper.globalEncryptedSharedPreferencesInstance
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
