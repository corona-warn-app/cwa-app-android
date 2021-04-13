package de.rki.coronawarnapp.coronatest.migration

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CoronaTestMigration @Inject constructor(
    private val submissionSettings: SubmissionSettings,
    private val tracingSettings: TracingSettings,
) {
    suspend fun startMigration(): Set<CoronaTest> {
        val token: RegistrationToken? = submissionSettings.registrationToken.value
        if (token == null) {
            Timber.tag(TAG).d("Nothing to migrate, token was null.")
            return emptySet()
        } else {
            Timber.tag(TAG).i("Migrating token %s", token)
        }

        val devicePairingSuccessfulAt = submissionSettings.devicePairingSuccessfulAt
        Timber.tag(TAG).v("devicePairingSuccessfulAt=%s", devicePairingSuccessfulAt)
        if (devicePairingSuccessfulAt == null) {
            if (CWADebug.isDeviceForTestersBuild) {
                throw IllegalStateException("Can't have registration token without device pairing timestamp !?")
            }
            return emptySet()
        }

        val isAllowedToSubmitKeys = submissionSettings.isAllowedToSubmitKeys
        Timber.tag(TAG).v("isAllowedToSubmitKeys=%s", isAllowedToSubmitKeys)

        val isSubmissionSuccessful = submissionSettings.isSubmissionSuccessful
        Timber.tag(TAG).v("isSubmissionSuccessful=%s", isSubmissionSuccessful)

        val hasViewedTestResult = submissionSettings.hasViewedTestResult.value
        Timber.tag(TAG).v("hasViewedTestResult=%s", hasViewedTestResult)

        val hasGivenConsent = submissionSettings.hasGivenConsent.value
        Timber.tag(TAG).v("hasGivenConsent=%s", hasGivenConsent)

        // TODO per test ?
        val testResultNotificationSent = tracingSettings.isTestResultAvailableNotificationSent
        Timber.tag(TAG).v("testResultNotificationSent=%s", testResultNotificationSent)

        val legacyPCRTest = PCRCoronaTest(
            testGUID = LEGACY_GUID,
            registrationToken = token,
            registeredAt = devicePairingSuccessfulAt,
            testResult = when (isAllowedToSubmitKeys) {
                true -> CoronaTestResult.PCR_POSITIVE
                else -> CoronaTestResult.PCR_OR_RAT_PENDING
            },
            isSubmitted = isSubmissionSuccessful,
            isViewed = hasViewedTestResult,
            isAdvancedConsentGiven = hasGivenConsent,
            isNotificationSent = testResultNotificationSent,
        )
        return setOf(legacyPCRTest).also {
            Timber.tag(TAG).d("Offering converted legacy CoronaTest: %s", it)
        }
    }

    suspend fun finishMigration() {
        Timber.tag(TAG).i("finishMigration()")
        submissionSettings.deleteLegacyTestData()
        tracingSettings.deleteLegacyTestData()
    }

    companion object {
        private const val TAG = "CoronaTestMigration"

        /**
         * We only use this for identification
         */
        private const val LEGACY_GUID = "legacy-guid"
    }
}
