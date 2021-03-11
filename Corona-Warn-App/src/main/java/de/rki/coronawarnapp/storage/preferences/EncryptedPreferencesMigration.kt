package de.rki.coronawarnapp.storage.preferences

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.SecurityHelper
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class EncryptedPreferencesMigration @Inject constructor(
    @AppContext private val context: Context,
    private val encryptedPreferencesHelper: EncryptedPreferencesHelper,
    private val cwaSettings: CWASettings,
    private val submissionSettings: SubmissionSettings,
    private val tracingSettings: TracingSettings,
    private val onboardingSettings: OnboardingSettings
) {

    fun doMigration() {
        Timber.d("Migration start")
        try {
            copyData()
            cleanData()
        } catch (e: Exception) {
            Timber.e(e, "Migration was not successful")
        }
        try {
            dropDatabase()
        } catch (e: Exception) {
            Timber.e(e, "Database removing was not successful")
        }
        Timber.d("Migration finish")
    }

    private fun copyData() {
        val encryptedSharedPreferences = SecurityHelper.globalEncryptedSharedPreferencesInstance ?: return
        Timber.d("EncryptedPreferences are available")
        SettingsLocalData(encryptedSharedPreferences).apply {
            cwaSettings.wasInteroperabilityShownAtLeastOnce = wasInteroperabilityShown()
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
            tracingSettings.initialPollingForTestResultTimeStamp = initialPollingForTestResultTimeStamp()
            tracingSettings.isTestResultAvailableNotificationSent = isTestResultAvailableNotificationSent()
            tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update { isUserToBeNotifiedOfLoweredRiskLevel() }
            tracingSettings.isConsentGiven = initialTracingActivationTimestamp() != 0L
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
    }

    private fun cleanData() {
        encryptedPreferencesHelper.clean()
    }

    private fun dropDatabase() {
        val file = context.getDatabasePath("coronawarnapp-db")
        if (file.exists()) {
            Timber.d("Removing database $file")
            SQLiteDatabase.deleteDatabase(file)
        }
    }

    private class SettingsLocalData(private val sharedPreferences: SharedPreferences) {

        fun wasInteroperabilityShown() = sharedPreferences.getBoolean(PREFERENCE_INTEROPERABILITY_WAS_USED, false)

        fun isNotificationsRiskEnabled(): Boolean = sharedPreferences.getBoolean(PKEY_NOTIFICATIONS_RISK_ENABLED, true)

        fun isNotificationsTestEnabled(): Boolean = sharedPreferences.getBoolean(PKEY_NOTIFICATIONS_TEST_ENABLED, true)

        fun numberOfRemainingSharePositiveTestResultReminders(): Int =
            sharedPreferences.getInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT, Int.MIN_VALUE)

        companion object {
            private const val PREFERENCE_INTEROPERABILITY_WAS_USED = "preference_interoperability_is_used_at_least_once"
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

        fun initialPollingForTestResultTimeStamp() = sharedPreferences.getLong(PKEY_POOLING_TEST_RESULT_STARTED, 0L)

        fun isTestResultAvailableNotificationSent() = sharedPreferences.getBoolean(PKEY_TEST_RESULT_NOTIFICATION, false)

        fun isUserToBeNotifiedOfLoweredRiskLevel() = sharedPreferences.getBoolean(PKEY_HAS_RISK_STATUS_LOWERED, false)

        fun initialTracingActivationTimestamp(): Long = sharedPreferences.getLong(PKEY_TRACING_ACTIVATION_TIME, 0L)

        companion object {
            private const val PKEY_POOLING_TEST_RESULT_STARTED = "preference_polling_test_result_started"
            private const val PKEY_TEST_RESULT_NOTIFICATION = "preference_test_result_notification"
            private const val PKEY_HAS_RISK_STATUS_LOWERED = "preference_has_risk_status_lowered"
            private const val PKEY_TRACING_ACTIVATION_TIME = "preference_initial_tracing_activation_time"
        }
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
}
