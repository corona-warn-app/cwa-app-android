package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionState
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestNegative
import de.rki.coronawarnapp.submission.ui.homecards.TestPending
import de.rki.coronawarnapp.submission.ui.homecards.TestPositive
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestResultDonor @Inject constructor(
    private val submissionStateProvider: SubmissionStateProvider,
    private val analyticsSettings: AnalyticsSettings,
    private val appConfigProvider: AppConfigProvider,
    private val riskLevelSettings: RiskLevelSettings,
    private val riskLevelStorage: RiskLevelStorage,
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val scannedAfterConsent = analyticsSettings.testScannedAfterConsent.value
        if (!scannedAfterConsent) {
            Timber.d("Skipping TestResultMetadata donation (testScannedAfterConsent=%s)", scannedAfterConsent)
            return TestResultMetadataNoContribution
        }

        val timestampAtRegistration = LocalData.initialTestResultReceivedTimestamp()

        if (timestampAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation timestampAtRegistration isn't found")
            return TestResultMetadataNoContribution
        }

        val configHours = appConfigProvider
            .getAppConfig()
            .analytics
            .hoursSinceTestRegistrationToSubmitTestResultMetadata

        val registrationTime = Instant.ofEpochMilli(timestampAtRegistration)
        val hoursSinceTestRegistrationTime = Duration(registrationTime, Instant.now()).standardHours.toInt()
        val isDiffHoursMoreThanConfigHoursForPendingTest = hoursSinceTestRegistrationTime >= configHours

        val submissionState = submissionStateProvider.state.first()

        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration =
            Duration(
                riskLevelSettings.lastChangeCheckedRiskLevelTimestamp,
                registrationTime
            ).standardDays.toInt()

        val riskLevelAtRegistration = analyticsSettings.riskLevelAtTestRegistration.value

        val hoursSinceHighRiskWarningAtTestRegistration =
            if (riskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_LOW) {
                DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING
            } else {
                calculatedHoursSinceHighRiskWarning(registrationTime)
            }

        return when {
            /**
             * If test is pending and
             * More than <hoursSinceTestRegistration> hours have passed since the test was registered,
             * it is included in the next submission and removed afterwards.
             * That means if the test result turns POS or NEG afterwards, this will not submitted
             */
            isDiffHoursMoreThanConfigHoursForPendingTest && submissionState.isPending ->
                pendingTestMetadataDonation(
                    hoursSinceTestRegistrationTime,
                    submissionState,
                    daysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    hoursSinceHighRiskWarningAtTestRegistration
                )

            /**
             * If the test result turns POSITIVE or NEGATIVE,
             * it is included in the next submission. Afterwards,
             * the collected metric data is removed.
             */
            submissionState.isFinal ->
                finalTestMetadataDonation(
                    registrationTime,
                    submissionState,
                    daysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    hoursSinceHighRiskWarningAtTestRegistration
                )
            else -> {
                Timber.d("Skipping Data donation")
                TestResultMetadataNoContribution
            }
        }
    }

    override suspend fun deleteData() = cleanUp()

    private fun cleanUp() {
        Timber.d("Cleaning data")
        with(analyticsSettings) {
            testScannedAfterConsent.update { false }
            riskLevelAtTestRegistration.update { PpaData.PPARiskLevel.RISK_LEVEL_UNKNOWN }
            finalTestResultReceivedAt.update { null }
        }
    }

    private fun pendingTestMetadataDonation(
        hoursSinceTestRegistrationTime: Int,
        submissionState: SubmissionState,
        daysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        hoursSinceHighRiskWarningAtTestRegistration: Int
    ): DonorModule.Contribution {
        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(hoursSinceHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setTestResult(submissionState.toPPATestResult())
            .setRiskLevelAtTestRegistration(analyticsSettings.riskLevelAtTestRegistration.value)
            .build()

        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private fun finalTestMetadataDonation(
        registrationTime: Instant,
        submissionState: SubmissionState,
        daysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        hoursSinceHighRiskWarningAtTestRegistration: Int
    ): DonorModule.Contribution {
        val finalTestResultReceivedAt = analyticsSettings.finalTestResultReceivedAt.value
        val hoursSinceTestRegistrationTime = if (finalTestResultReceivedAt != null) {
            Duration(registrationTime, finalTestResultReceivedAt).standardHours.toInt()
        } else {
            DEFAULT_HOURS_SINCE_TEST_REGISTRATION_TIME
        }

        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(hoursSinceHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setTestResult(submissionState.toPPATestResult())
            .setRiskLevelAtTestRegistration(analyticsSettings.riskLevelAtTestRegistration.value)
            .build()

        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private suspend fun calculatedHoursSinceHighRiskWarning(registrationTime: Instant): Int {
        val highRiskResultCalculatedAt = riskLevelStorage
            .latestAndLastSuccessful
            .first()
            .filter { it.isIncreasedRisk }
            .minByOrNull { it.calculatedAt }
            ?.calculatedAt ?: return DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING

        return Duration(
            highRiskResultCalculatedAt,
            registrationTime
        ).standardHours.toInt()
    }

    private inline val SubmissionState.isFinal: Boolean get() = this is TestNegative || this is TestPositive
    private inline val SubmissionState.isPending get() = this is TestPending

    private fun SubmissionState.toPPATestResult(): PpaData.PPATestResult {
        return when (this) {
            is TestPending -> PpaData.PPATestResult.TEST_RESULT_PENDING
            is TestPositive -> PpaData.PPATestResult.TEST_RESULT_POSITIVE
            is TestNegative -> PpaData.PPATestResult.TEST_RESULT_NEGATIVE
            else -> PpaData.PPATestResult.TEST_RESULT_UNKNOWN
        }
    }

    data class TestResultMetadataContribution(
        private val testResultMetadata: PpaData.PPATestResultMetadata,
        private val onFinishDonation: suspend () -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addTestResultMetadataSet(testResultMetadata)
        }

        override suspend fun finishDonation(successful: Boolean) = onFinishDonation()
    }

    object TestResultMetadataNoContribution : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) = Unit
        override suspend fun finishDonation(successful: Boolean) = Unit
    }

    companion object {
        private const val DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING = -1
        private const val DEFAULT_HOURS_SINCE_TEST_REGISTRATION_TIME = 0
    }
}
