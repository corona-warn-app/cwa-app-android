package de.rki.coronawarnapp.storage.preferences

import android.content.SharedPreferences
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.EncryptedPreferences
import timber.log.Timber
import javax.inject.Inject

class EncryptedPreferencesMigration @Inject constructor(
    private val encryptedPreferencesHelper: EncryptedPreferencesHelper,
    private val cwaSettings: CWASettings,
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
                // TODO: onboarding migration
            }

            TracingLocalData(encryptedSharedPreferences).apply {
                // TODO: tracing migration
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
        // TODO:
    }

    private class TracingLocalData(private val sharedPreferences: SharedPreferences) {
        // TODO:
    }

    private class SubmissionLocalData(private val sharedPreferences: SharedPreferences) {
        // TODO:
    }
}
