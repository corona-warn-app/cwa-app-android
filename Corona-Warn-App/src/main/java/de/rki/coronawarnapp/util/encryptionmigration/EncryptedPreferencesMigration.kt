package de.rki.coronawarnapp.util.encryptionmigration

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantOrNull
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject

class EncryptedPreferencesMigration @Inject constructor(
    @AppContext private val context: Context,
    private val encryptedPreferences: EncryptedPreferencesHelper,
    private val cwaSettings: CWASettings,
    private val submissionSettings: SubmissionSettings,
    private val tracingSettings: TracingSettings,
    private val onboardingSettings: OnboardingSettings,
    private val errorResetTool: EncryptionErrorResetTool,
    dispatcherProvider: DispatcherProvider,
) {

    private val coroutineScope: CoroutineScope = CoroutineScope(dispatcherProvider.IO)

    fun doMigration() {
        coroutineScope.launch {
            Timber.d("Migration start")
            try {
                encryptedPreferences.instance?.let { copyData(it) }
            } catch (e: Exception) {
                e.reportProblem(tag = this::class.simpleName, info = "Migration failed")
                errorResetTool.updateIsResetNoticeToBeShown(true)
            } finally {
                try {
                    encryptedPreferences.clean()
                } catch (e: Exception) {
                    e.reportProblem(tag = this::class.simpleName, info = "Encryption data clean up failed")
                }
            }
            try {
                dropDatabase()
            } catch (e: Exception) {
                e.reportProblem(tag = this::class.simpleName, info = "Database removing failed")
            }
            Timber.d("Migration finish")
        }
    }

    private suspend fun copyData(encryptedSharedPreferences: SharedPreferences) {
        Timber.i("copyData(): EncryptedPreferences are available")
        SettingsLocalData(encryptedSharedPreferences).apply {
            cwaSettings.updateWasInteroperabilityShownAtLeastOnce(wasInteroperabilityShown())
            cwaSettings.updateWasTracingExplanationDialogShown(wasTracingExplanationDialogShown())
            cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersPcr(
                numberOfRemainingSharePositiveTestResultReminders()
            )
        }

        OnboardingLocalData(encryptedSharedPreferences).apply {
            coroutineScope.launch {
                onboardingSettings.updateOnboardingCompletedTimestamp(
                    timeStamp = onboardingCompletedTimestamp()?.let { Instant.ofEpochMilli(it) }
                )
                onboardingSettings.updateBackgroundCheckDone(isDone = isBackgroundCheckDone())
            }
        }

        TracingLocalData(encryptedSharedPreferences).apply {
            coroutineScope.launch {
                tracingSettings.updateTestResultAvailableNotificationSentMigration(
                    isTestResultAvailableNotificationSent()
                )
                tracingSettings.updateUserToBeNotifiedOfLoweredRiskLevel(isUserToBeNotifiedOfLoweredRiskLevel())
                tracingSettings.updateConsentGiven(isConsentGiven = initialTracingActivationTimestamp() != 0L)
            }
        }

        SubmissionLocalData(encryptedSharedPreferences).apply {
            submissionSettings.updateRegistrationTokenMigration(registrationToken())
            submissionSettings.updateInitialTestResultReceivedAtMigration(
                initialTestResultReceivedTimestamp().toInstantOrNull()
            )
            submissionSettings.updateDevicePairingSuccessfulAtMigration(
                devicePairingSuccessfulTimestamp().toInstantOrNull()
            )
            submissionSettings.updateIsSubmissionSuccessfulMigration(numberOfSuccessfulSubmissions() >= 1)
            submissionSettings.updateIsAllowedToSubmitKeysMigration(isAllowedToSubmitDiagnosisKeys())
        }
        Timber.i("copyData(): EncryptedPreferences have been copied.")
    }

    private fun dropDatabase() {
        val file = context.getDatabasePath("coronawarnapp-db")
        if (!file.exists()) {
            Timber.d("Encrypted database does not exist.")
            return
        }

        Timber.i("Removing database $file")
        if (SQLiteDatabase.deleteDatabase(file)) {
            Timber.i("Legacy encrypted database was deleted.")
        } else {
            Timber.e("Legacy encrypted database could not be deleted.")
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    class SettingsLocalData(private val sharedPreferences: SharedPreferences) {

        fun wasInteroperabilityShown() = sharedPreferences.getBoolean(PKEY_INTEROPERABILITY_WAS_USED, false)

        fun wasTracingExplanationDialogShown() = sharedPreferences.getBoolean(PKEY_TRACING_EXPLANATION_WAS_SHOWN, false)

        fun numberOfRemainingSharePositiveTestResultReminders(): Int =
            sharedPreferences.getInt(PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT, Int.MIN_VALUE)

        companion object {
            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_INTEROPERABILITY_WAS_USED = "preference_interoperability_is_used_at_least_once"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_TRACING_EXPLANATION_WAS_SHOWN = "preference_risk_days_explanation_shown"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_NOTIFICATIONS_RISK_ENABLED = "preference_notifications_risk_enabled"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_NOTIFICATIONS_TEST_ENABLED = "preference_notifications_test_enabled"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT = "preference_positive_test_result_reminder_count"
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    class OnboardingLocalData(private val sharedPreferences: SharedPreferences) {
        fun onboardingCompletedTimestamp(): Long? {
            val timestamp = sharedPreferences.getLong(PKEY_ONBOARDING_COMPLETED_TIMESTAMP, 0L)

            if (timestamp == 0L) return null
            return timestamp
        }

        fun isBackgroundCheckDone(): Boolean = sharedPreferences.getBoolean(PKEY_BACKGROUND_CHECK_DONE, false)

        companion object {
            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_ONBOARDING_COMPLETED_TIMESTAMP = "preference_onboarding_completed_timestamp"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_BACKGROUND_CHECK_DONE = "preference_background_check_done"
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    class TracingLocalData(private val sharedPreferences: SharedPreferences) {

        fun isTestResultAvailableNotificationSent() = sharedPreferences.getBoolean(PKEY_TEST_RESULT_NOTIFICATION, false)

        fun isUserToBeNotifiedOfLoweredRiskLevel() = sharedPreferences.getBoolean(PKEY_HAS_RISK_STATUS_LOWERED, false)

        fun initialTracingActivationTimestamp(): Long = sharedPreferences.getLong(PKEY_TRACING_ACTIVATION_TIME, 0L)

        companion object {
            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_TEST_RESULT_NOTIFICATION = "preference_test_result_notification"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_HAS_RISK_STATUS_LOWERED = "preference_has_risk_status_lowered"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_TRACING_ACTIVATION_TIME = "preference_initial_tracing_activation_time"
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    class SubmissionLocalData(private val sharedPreferences: SharedPreferences) {
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
            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_REGISTRATION_TOKEN = "preference_registration_token"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_INITIAL_RESULT_RECEIVED_TIME = "preference_initial_result_received_time"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_DEVICE_PARING_SUCCESSFUL_TIME = "preference_device_pairing_successful_time"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_NUMBER_SUCCESSFUL_SUBMISSIONS = "preference_number_successful_submissions"

            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            const val PKEY_IS_ALLOWED_TO_SUBMIT = "preference_is_allowed_to_submit_diagnosis_keys"
        }
    }
}
