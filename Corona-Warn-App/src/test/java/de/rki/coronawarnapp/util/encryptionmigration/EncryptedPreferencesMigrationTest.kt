package de.rki.coronawarnapp.util.encryptionmigration

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.MockSharedPreferences
import testhelpers.preferences.mockFlowPreference
import java.io.File

@Suppress("DEPRECATION")
class EncryptedPreferencesMigrationTest : BaseIOTest() {
    @MockK lateinit var context: Context
    @MockK lateinit var encryptedPreferencesHelper: EncryptedPreferencesHelper
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var encryptedErrorResetTool: EncryptionErrorResetTool

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val dbFile = File(testDir, "database.sql")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testDir.mkdirs()
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    private fun createInstance() = EncryptedPreferencesMigration(
        context = context,
        encryptedPreferences = encryptedPreferencesHelper,
        cwaSettings = cwaSettings,
        submissionSettings = submissionSettings,
        tracingSettings = tracingSettings,
        onboardingSettings = onboardingSettings,
        errorResetTool = encryptedErrorResetTool
    )

    private fun createOldPreferences() = MockSharedPreferences().also {
        it.edit {
            // SettingsLocalData
            putBoolean(EncryptedPreferencesMigration.SettingsLocalData.PKEY_INTEROPERABILITY_WAS_USED, true)
            putBoolean(EncryptedPreferencesMigration.SettingsLocalData.PKEY_TRACING_EXPLANATION_WAS_SHOWN, true)
            putBoolean(EncryptedPreferencesMigration.SettingsLocalData.PKEY_NOTIFICATIONS_RISK_ENABLED, false)
            putBoolean(EncryptedPreferencesMigration.SettingsLocalData.PKEY_NOTIFICATIONS_TEST_ENABLED, false)
            putInt(
                EncryptedPreferencesMigration.SettingsLocalData.PKEY_POSITIVE_TEST_RESULT_REMINDER_COUNT,
                Int.MAX_VALUE
            )

            // OnboardingLocalData
            putLong(EncryptedPreferencesMigration.OnboardingLocalData.PKEY_ONBOARDING_COMPLETED_TIMESTAMP, 10101010L)
            putBoolean(EncryptedPreferencesMigration.OnboardingLocalData.PKEY_BACKGROUND_CHECK_DONE, true)

            // TracingLocalData
            putLong(EncryptedPreferencesMigration.TracingLocalData.PKEY_POOLING_TEST_RESULT_STARTED, 10101010L)
            putBoolean(EncryptedPreferencesMigration.TracingLocalData.PKEY_TEST_RESULT_NOTIFICATION, true)
            putBoolean(EncryptedPreferencesMigration.TracingLocalData.PKEY_HAS_RISK_STATUS_LOWERED, true)
            putLong(EncryptedPreferencesMigration.TracingLocalData.PKEY_TRACING_ACTIVATION_TIME, 10101010L)

            // SubmissionLocalData
            putString(EncryptedPreferencesMigration.SubmissionLocalData.PKEY_REGISTRATION_TOKEN, "super_secret_token")
            putLong(EncryptedPreferencesMigration.SubmissionLocalData.PKEY_INITIAL_RESULT_RECEIVED_TIME, 10101010L)
            putLong(EncryptedPreferencesMigration.SubmissionLocalData.PKEY_DEVICE_PARING_SUCCESSFUL_TIME, 10101010L)
            putInt(EncryptedPreferencesMigration.SubmissionLocalData.PKEY_NUMBER_SUCCESSFUL_SUBMISSIONS, 1)
            putBoolean(EncryptedPreferencesMigration.SubmissionLocalData.PKEY_IS_ALLOWED_TO_SUBMIT, true)
        }
    }

    @Test
    fun `is migration successful`() {
        every { context.getDatabasePath("coronawarnapp-db") } returns dbFile
        every { encryptedPreferencesHelper.clean() } just Runs

        val oldPreferences = createOldPreferences()
        every { encryptedPreferencesHelper.instance } returns oldPreferences

        // SettingsLocalData
        every { cwaSettings.wasInteroperabilityShownAtLeastOnce = true } just Runs
        every { cwaSettings.wasTracingExplanationDialogShown = true } just Runs
        val mockRiskPreference = mockFlowPreference(true)
        every { cwaSettings.isNotificationsRiskEnabled } returns mockRiskPreference
        val mockTestPreference = mockFlowPreference(true)
        every { cwaSettings.isNotificationsTestEnabled } returns mockTestPreference
        every { cwaSettings.numberOfRemainingSharePositiveTestResultReminders = Int.MAX_VALUE } just Runs

        // OnboardingLocalData
        every { onboardingSettings.onboardingCompletedTimestamp = Instant.ofEpochMilli(10101010L) } just Runs
        every { onboardingSettings.isBackgroundCheckDone = true } just Runs

        // TracingLocalData
        every { tracingSettings.initialPollingForTestResultTimeStampMigration = 10101010L } just Runs
        every { tracingSettings.isTestResultAvailableNotificationSentMigration = true } just Runs
        val mockNotificationPreference = mockFlowPreference(false)
        every { tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel } returns mockNotificationPreference
        every { tracingSettings.isConsentGiven = true } just Runs

        // SubmissionLocalData
        every { submissionSettings.registrationTokenMigration = any() } just Runs
        every { submissionSettings.initialTestResultReceivedAtMigration = Instant.ofEpochMilli(10101010L) } just Runs
        every { submissionSettings.devicePairingSuccessfulAtMigration = Instant.ofEpochMilli(10101010L) } just Runs
        every { submissionSettings.isSubmissionSuccessfulMigration = true } just Runs
        every { submissionSettings.isAllowedToSubmitKeysMigration = true } just Runs

        val migrationInstance = createInstance()

        migrationInstance.doMigration()

        // SettingsLocalData
        mockRiskPreference.value shouldBe false
        mockTestPreference.value shouldBe false

        // TracingLocalData
        mockNotificationPreference.value shouldBe true

        // SubmissionLocalData
        verify { submissionSettings.registrationTokenMigration = "super_secret_token" }
    }

    @Test
    fun `error during migration will be caught`() {
        every { context.getDatabasePath("coronawarnapp-db") } returns dbFile

        val mockPrefs = mockk<SharedPreferences>()
        every {
            mockPrefs.getBoolean(
                EncryptedPreferencesMigration.SettingsLocalData.PKEY_INTEROPERABILITY_WAS_USED,
                false
            )
        } throws Exception("No one expects the spanish inquisition")

        every { encryptedPreferencesHelper.instance } returns mockPrefs
        every { encryptedPreferencesHelper.clean() } just Runs

        every { encryptedErrorResetTool.isResetNoticeToBeShown = true } just Runs

        shouldNotThrowAny {
            val instance = createInstance()
            instance.doMigration()
        }
    }
}
