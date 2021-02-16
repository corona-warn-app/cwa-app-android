package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
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

        val submissionState = submissionStateProvider.state.first()
        val isTestResultReceived = submissionState is TestPositive ||
            submissionState is TestNegative

        val configHours = appConfigProvider
            .getAppConfig()
            .analytics
            .hoursSinceTestRegistrationToSubmitTestResultMetadata

        val registrationTime = Instant.ofEpochMilli(timestampAtRegistration)
        val hoursSinceTestRegistrationTime = Duration(registrationTime, Instant.now()).standardHours.toInt()
        val isHoursDiffAcceptable = hoursSinceTestRegistrationTime >= configHours

        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration =
            Duration(
                riskLevelSettings.lastChangeCheckedRiskLevelTimestamp,
                registrationTime
            ).standardDays.toInt()

        return if (isHoursDiffAcceptable && isTestResultReceived) {
            val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
                .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
                // TODO verify setters below
                .setHoursSinceHighRiskWarningAtTestRegistration(0)
                .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                    daysSinceMostRecentDateAtRiskLevelAtTestRegistration
                )
                .setTestResult(submissionState.toPPATestResult())
                .setRiskLevelAtTestRegistration(analyticsSettings.riskLevelAtTestRegistration.value)
                .build()

            TestResultMetadataContribution(testResultMetaData, ::cleanUp)
        } else {
            Timber.d(
                "Skipping Data Donation (isHoursDiffAcceptable=%s,isTestResultReceived=%s)",
                isHoursDiffAcceptable,
                isTestResultReceived
            )
            TestResultMetadataNoContribution
        }
    }

    override suspend fun deleteData() = cleanUp()

    private fun cleanUp() {
        with(analyticsSettings) {
            testScannedAfterConsent.update { false }
            riskLevelAtTestRegistration.update { PpaData.PPARiskLevel.RISK_LEVEL_UNKNOWN }
        }
    }

    private fun SubmissionState.isValid(): Boolean {
        return this is TestNegative || this is TestPositive || this is TestPending
    }

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
}
