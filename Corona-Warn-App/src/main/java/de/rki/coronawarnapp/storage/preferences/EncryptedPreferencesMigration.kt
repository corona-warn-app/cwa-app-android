package de.rki.coronawarnapp.storage.preferences

import android.content.SharedPreferences
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class EncryptedPreferencesMigration @Inject constructor(
    private val encryptedPreferencesHelper: EncryptedPreferencesHelper,
    private val cwaSettings: CWASettings,
    private val submissionSettings: SubmissionSettings,
    private val onboardingSettings: OnboardingSettings,
    @EncryptedPreferences private val encryptedSharedPreferences: SharedPreferences?
) {

    fun doMigration() {
        try {
            copyData()
        } catch (e: Exception) {
            Timber.e(e, "Migration was not successful")
        }
    }

    private fun copyData() {
        Timber.d("EncryptedPreferencesMigration START")
        if (encryptedSharedPreferences != null && encryptedPreferencesHelper.isAvailable()) {
            Timber.d("EncryptedPreferences are available")
            SettingsLocalData(encryptedSharedPreferences).apply {
                cwaSettings.isNotificationsRiskEnabled.update { isNotificationsRiskEnabled() }
                cwaSettings.isNotificationsTestEnabled.update { isNotificationsTestEnabled() }
                cwaSettings.numberOfRemainingSharePositiveTestResultReminders =
                    numberOfRemainingSharePositiveTestResultReminders()
            }

            OnboardingLocalData(encryptedSharedPreferences).apply {
                onboardingSettings.onboardingCompletedTimestamp = onboardingCompletedTimestamp()?.let {
                    Instant.ofEpochMilli(it)
                }
                onboardingSettings.isBackgroundCheckDone = isBackgroundCheckDone()
            }

            TracingLocalData(encryptedSharedPreferences).apply {
                // TODO: tracing migration
            }

            SubmissionLocalData(encryptedSharedPreferences).apply {
                submissionSettings.registrationToken.update {
                    registrationToken()
                }
                submissionSettings.initialTestResultReceivedAt = initialTestResultReceivedTimestamp().toInstantOrNull()
                submissionSettings.devicePairingSuccessfulAt = devicePairingSuccessfulTimestamp().toInstantOrNull()
                submissionSettings.isSubmissionSuccessful = numberOfSuccessfulSubmissions() >= 1
                submissionSettings.isAllowedToSubmitKeys = isAllowedToSubmitDiagnosisKeys()
            }

            encryptedPreferencesHelper.clean()
        }
        Timber.d("EncryptedPreferencesMigration END")
    }

    // Moving LocalData methods here to prevent accidental misuse.
    // TODO: delete or update LocalData, SecurityConstants, SecurityHelper after all methods are migrated
    private class SettingsLocalData(private val sharedPreferences: SharedPreferences) {

        fun isNotificationsRiskEnabled(): Boolean = sharedPreferences.getBoolean(PKEY_NOTIFICATIONS_RISK_ENABLED, true)

        fun isNotificationsTestEnabled(): Boolean = sharedPreferences.getBoolean(PKEY_NOTIFICATIONS_TEST_ENABLED, true)

        fun numberOfRemainingSharePositiveTestResultReminders(): Int =
            sharedPreferences.getInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT, Int.MIN_VALUE)

        companion object {
            private const val PKEY_NOTIFICATIONS_RISK_ENABLED = "preference_notifications_risk_enabled"
            private const val PKEY_NOTIFICATIONS_TEST_ENABLED = "preference_notifications_test_enabled"
            private const val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT =
                "preference_positive_test_result_reminder_count"
        }
    }

    private class OnboardingLocalData(private val sharedPreferences: SharedPreferences) {
        fun onboardingCompletedTimestamp(): Long? {
            val timestamp = sharedPreferences.getLong(PKEY_ONBOARDING_COMPLETED_TIMESTAMP, 0L)

            if (timestamp == 0L) return null
            return timestamp
        }

        fun isBackgroundCheckDone(): Boolean = sharedPreferences.getBoolean(PKEY_BACKGROUND_CHECK_DONE, false)

        companion object {
            private const val PKEY_ONBOARDING_COMPLETED_TIMESTAMP = "preference_onboarding_completed_timestamp"
            private const val PKEY_BACKGROUND_CHECK_DONE = "preference_background_check_done"
        }
    }

    private class TracingLocalData(private val sharedPreferences: SharedPreferences) {
        // TODO:
    }

    private class SubmissionLocalData(private val sharedPreferences: SharedPreferences) {
        fun registrationToken(): String? = sharedPreferences.getString(PKEY_REGISTRATION_TOKEN, null)

        fun initialTestResultReceivedTimestamp(): Long? {
            val timestamp = sharedPreferences.getLong(PKEY_INITIAL_RESULT_RECEIVED_TIME, 0L)

            if (timestamp == 0L) return null
            return timestamp
        }

        fun devicePairingSuccessfulTimestamp(): Long = sharedPreferences.getLong(PKEY_DEVICE_PARING_SUCCESSFUL_TIME, 0L)

        fun numberOfSuccessfulSubmissions(): Int = sharedPreferences.getInt(PKEY_NUMBER_SUCCESSFUL_SUBMISSIONS, 0)

        fun isAllowedToSubmitDiagnosisKeys(): Boolean = sharedPreferences.getBoolean(PKEY_IS_ALLOWED_TO_SUBMIT, false)

        companion object {
            private const val PKEY_REGISTRATION_TOKEN = "preference_registration_token"
            private const val PKEY_INITIAL_RESULT_RECEIVED_TIME = "preference_initial_result_received_time"
            private const val PKEY_DEVICE_PARING_SUCCESSFUL_TIME = "preference_device_pairing_successful_time"
            private const val PKEY_NUMBER_SUCCESSFUL_SUBMISSIONS = "preference_number_successful_submissions"
            private const val PKEY_IS_ALLOWED_TO_SUBMIT = "preference_is_allowed_to_submit_diagnosis_keys"
        }
    }

    private fun Long?.toInstantOrNull(): Instant? =
        if (this != null && this != 0L) {
            Instant.ofEpochMilli(this)
        } else null
}
