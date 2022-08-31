package de.rki.coronawarnapp.coronatest.migration

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PCRTestMigration @Inject constructor(
    private val submissionSettings: SubmissionSettings,
    private val tracingSettings: TracingSettings,
) {

    private val mutex = Mutex()
    private var isMigrating: Boolean = false

    @Suppress("DEPRECATION")
    suspend fun startMigration(): Set<PersonalCoronaTest> = mutex.withLock {
        if (isMigrating) throw IllegalStateException("Migration already in progress")
        isMigrating = true
        Timber.tag(TAG).i("startMigration()")

        val token: RegistrationToken? = submissionSettings.registrationTokenMigration
        if (token == null) {
            Timber.tag(TAG).d("Nothing to migrate, token was null.")
            return emptySet()
        } else {
            Timber.tag(TAG).i("Migrating token %s", token)
        }

        val devicePairingSuccessfulAt = submissionSettings.devicePairingSuccessfulAtMigration
        Timber.tag(TAG).v("devicePairingSuccessfulAt=%s", devicePairingSuccessfulAt)
        if (devicePairingSuccessfulAt == null) {
            if (CWADebug.isDeviceForTestersBuild) {
                throw IllegalStateException("Can't have registration token without device pairing timestamp !?")
            }
            return emptySet()
        }

        val isAllowedToSubmitKeys = submissionSettings.isAllowedToSubmitKeysMigration
        Timber.tag(TAG).v("isAllowedToSubmitKeys=%s", isAllowedToSubmitKeys)

        val isSubmissionSuccessful = submissionSettings.isSubmissionSuccessfulMigration
        Timber.tag(TAG).v("isSubmissionSuccessful=%s", isSubmissionSuccessful)

        val hasViewedTestResult = submissionSettings.hasViewedTestResultMigration
        Timber.tag(TAG).v("hasViewedTestResult=%s", hasViewedTestResult)

        val hasGivenConsent = submissionSettings.hasGivenConsentMigration
        Timber.tag(TAG).v("hasGivenConsent=%s", hasGivenConsent)

        val testResultNotificationSent = tracingSettings.isTestResultAvailableNotificationSentMigration()
        Timber.tag(TAG).v("testResultNotificationSent=%s", testResultNotificationSent)

        val legacyPCRTest = PCRCoronaTest(
            identifier = LEGACY_IDENTIFIER,
            registrationToken = token,
            registeredAt = devicePairingSuccessfulAt,
            lastUpdatedAt = devicePairingSuccessfulAt,
            testResult = when (isAllowedToSubmitKeys) {
                true -> CoronaTestResult.PCR_POSITIVE
                else -> CoronaTestResult.PCR_OR_RAT_PENDING
            },
            testResultReceivedAt = devicePairingSuccessfulAt,
            isSubmitted = isSubmissionSuccessful,
            isViewed = hasViewedTestResult,
            isAdvancedConsentGiven = hasGivenConsent,
            isResultAvailableNotificationSent = testResultNotificationSent,
        )
        return setOf(legacyPCRTest).also {
            Timber.tag(TAG).d("Offering converted legacy CoronaTest: %s", it)
        }
    }

    suspend fun finishMigration() = mutex.withLock {
        if (!isMigrating) return@withLock
        isMigrating = false
        Timber.tag(TAG).i("finishMigration()")

        submissionSettings.deleteLegacyTestData()
        tracingSettings.deleteLegacyTestData()
    }

    companion object {
        private const val TAG = "CoronaTestMigration"

        /**
         * We only use this for identification, needs to be guaranteed different from any non-legacy identifiers.
         */
        private const val LEGACY_IDENTIFIER = "qrcode-pcr-legacy"
    }
}
