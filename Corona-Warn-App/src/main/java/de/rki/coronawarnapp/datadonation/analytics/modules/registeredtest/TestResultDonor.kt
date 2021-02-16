package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestNegative
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
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        if (!analyticsSettings.testScannedAfterConsent.value) {
            Timber.d("Skipping TestResultMetadata donation(Test scanned before consent) ")
            return TestResultMetadataNoContribution
        }

        val submissionState = submissionStateProvider.state.first()
        val isTestResultReceived = submissionState is TestPositive ||
            submissionState is TestNegative

        val configHours = appConfigProvider
            .getAppConfig()
            .analytics
            .hoursSinceTestRegistrationToSubmitTestResultMetadata
        val registrationTime = Instant.ofEpochMilli(LocalData.initialPollingForTestResultTimeStamp())
        val hoursSinceTestRegistrationTime = Duration(registrationTime, Instant.now()).standardHours.toInt()
        val isHoursDiffAcceptable = hoursSinceTestRegistrationTime >= configHours

        return if (isHoursDiffAcceptable && isTestResultReceived) {
            val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
                .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
                // TODO verify setters below
                .setHoursSinceHighRiskWarningAtTestRegistration(0)
                .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(0)
                .setTestResult(PpaData.PPATestResult.TEST_RESULT_POSITIVE)
                .setRiskLevelAtTestRegistration(PpaData.PPARiskLevel.RISK_LEVEL_LOW)
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
            riskLevelAtTestRegistration.update { null }
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
