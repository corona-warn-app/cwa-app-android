package de.rki.coronawarnapp.util.encryptionmigration

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import kotlinx.coroutines.test.runTest
import testhelpers.TestDispatcherProvider
import testhelpers.preferences.MockSharedPreferences
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
        errorResetTool = encryptedErrorResetTool,
        dispatcherProvider = TestDispatcherProvider()
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
    fun `is migration successful`() = runTest {
        every { context.getDatabasePath("coronawarnapp-db") } returns dbFile
        every { encryptedPreferencesHelper.clean() } just Runs

        val oldPreferences = createOldPreferences()
        every { encryptedPreferencesHelper.instance } returns oldPreferences

        // SettingsLocalData
        coEvery { cwaSettings.updateWasInteroperabilityShownAtLeastOnce(true) } just Runs
        coEvery { cwaSettings.updateWasTracingExplanationDialogShown(true) } just Runs
        coEvery { cwaSettings.updateNumberOfRemainingSharePositiveTestResultRemindersPcr(Int.MAX_VALUE) } just Runs

        // OnboardingLocalData
        val mockOnboardingCompletedTimestamp = flowOf(Instant.ofEpochMilli(10101010L))
        every { onboardingSettings.onboardingCompletedTimestamp } returns mockOnboardingCompletedTimestamp
        coEvery { onboardingSettings.updateBackgroundCheckDone(isDone = true) } just Runs
        coEvery { onboardingSettings.updateOnboardingCompletedTimestamp(any()) } just Runs

        // TracingLocalData
        coEvery { tracingSettings.updateTestResultAvailableNotificationSentMigration(sent = true) } just Runs
        every { tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel } returns flowOf(false)
        coEvery { tracingSettings.updateConsentGiven(isConsentGiven = true) } just Runs
        coEvery { tracingSettings.updateUserToBeNotifiedOfLoweredRiskLevel(any()) } just Runs

        // SubmissionLocalData
        coEvery { submissionSettings.updateRegistrationTokenMigration(any()) } just Runs
        coEvery {
            submissionSettings.updateInitialTestResultReceivedAtMigration(Instant.ofEpochMilli(10101010L))
        } just Runs
        coEvery {
            submissionSettings.updateDevicePairingSuccessfulAtMigration(Instant.ofEpochMilli(10101010L))
        } just Runs
        coEvery { submissionSettings.updateIsSubmissionSuccessfulMigration(true) } just Runs
        coEvery { submissionSettings.updateIsAllowedToSubmitKeysMigration(true) } just Runs

        val migrationInstance = createInstance()

        migrationInstance.doMigration()

        coVerify { tracingSettings.updateUserToBeNotifiedOfLoweredRiskLevel(notify = true) }

        // SubmissionLocalData
        coVerify { submissionSettings.updateRegistrationTokenMigration("super_secret_token") }
    }

    @Test
    fun `error during migration will be caught`() = runTest {
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

        coEvery { encryptedErrorResetTool.updateIsResetNoticeToBeShown(any()) } just Runs

        shouldNotThrowAny {
            val instance = createInstance()
            instance.doMigration()
        }
    }
}
