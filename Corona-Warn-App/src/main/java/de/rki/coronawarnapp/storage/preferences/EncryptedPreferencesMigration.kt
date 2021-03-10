package de.rki.coronawarnapp.storage.preferences

import android.content.SharedPreferences
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class EncryptedPreferencesMigration @Inject constructor(
    private val encryptedPreferencesHelper: EncryptedPreferencesHelper,
    private val cwaSettings: CWASettings,
    private val tracingSettings: TracingSettings,
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

            val submissionLocalData = SubmissionLocalData(encryptedSharedPreferences).apply {
                // TODO: submission migration
            }

            encryptedPreferencesHelper.clean()
        }
        Timber.d("EncryptedPreferencesMigration END")
    }

    // Moving LocalData methods here to prevent accidental misuse.
    // TODO: delete or update LocalData, SecurityConstants, SecurityHelper after all methods are migrated
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
        // TODO:
    }
}
