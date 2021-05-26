package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.common.isFinal
import de.rki.coronawarnapp.datadonation.analytics.common.isPending
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRTestResultDonor @Inject constructor(
    testResultSettings: AnalyticsPCRTestResultSettings,
    timeStamper: TimeStamper,
) : AnalyticsTestResultDonor(testResultSettings, timeStamper)

@Singleton
class AnalyticsRATestResultDonor @Inject constructor(
    testResultSettings: AnalyticsRATestResultSettings,
    timeStamper: TimeStamper,
) : AnalyticsTestResultDonor(testResultSettings, timeStamper)

abstract class AnalyticsTestResultDonor(
    private val testResultSettings: AnalyticsTestResultSettings,
    private val timeStamper: TimeStamper,
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val scannedAfterConsent = testResultSettings.testScannedAfterConsent.value
        if (!scannedAfterConsent) {
            Timber.d("Skipping TestResultMetadata donation (scannedAfterConsent=%s)", scannedAfterConsent)
            return TestResultMetadataNoContribution
        }

        val timestampAtRegistration = testResultSettings.testRegisteredAt.value
        if (timestampAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation (timestampAtRegistration is missing)")
            return TestResultMetadataNoContribution
        }

        val testResultAtRegistration = testResultSettings.testResultAtRegistration.value
        if (testResultAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation (testResultAtRegistration is missing)")
            return TestResultMetadataNoContribution
        }

        Timber.i(
            "mostRecentDateWithHighOrLowRiskLevel=%s,timestampAtRegistration=%s",
            testResultSettings.ewMostRecentDateWithHighOrLowRiskLevel.value,
            timestampAtRegistration
        )

        val configHours = request.currentConfig.analytics.hoursSinceTestRegistrationToSubmitTestResultMetadata
        val hoursSinceTestRegistrationTime = Duration(timestampAtRegistration, timeStamper.nowUTC).standardHours.toInt()
        val isDiffHoursMoreThanConfigHoursForPendingTest = hoursSinceTestRegistrationTime >= configHours
        Timber.i("hoursSinceTestRegistrationTime=$hoursSinceTestRegistrationTime, configHours=$configHours")

        return when {
            /**
             * If test is pending and
             * More than <hoursSinceTestRegistration> hours have passed since the test was registered,
             * it is included in the next submission and removed afterwards.
             * That means if the test result turns POS or NEG afterwards, this will not submitted
             */
            testResultAtRegistration.isPending && isDiffHoursMoreThanConfigHoursForPendingTest ->
                pendingTestMetadataDonation(
                    hoursSinceTestRegistrationTime = hoursSinceTestRegistrationTime,
                    testResult = testResultAtRegistration,
                    ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration =
                    testResultSettings.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value,
                    ewHoursSinceHighRiskWarningAtTestRegistration =
                    testResultSettings.ewHoursSinceHighRiskWarningAtTestRegistration.value,
                    ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration =
                    testResultSettings.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value,
                    ptHoursSinceHighRiskWarningAtTestRegistration =
                    testResultSettings.ptHoursSinceHighRiskWarningAtTestRegistration.value
                )

            /**
             * If the test result turns POSITIVE or NEGATIVE,
             * it is included in the next submission. Afterwards,
             * the collected metric data is removed.
             */
            testResultAtRegistration.isFinal ->
                finalTestMetadataDonation(
                    timestampAtRegistration,
                    testResultAtRegistration,
                    testResultSettings.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value,
                    testResultSettings.ewHoursSinceHighRiskWarningAtTestRegistration.value
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
        testResultSettings.clear()
    }

    private fun pendingTestMetadataDonation(
        hoursSinceTestRegistrationTime: Int,
        testResult: CoronaTestResult,
        ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        ewHoursSinceHighRiskWarningAtTestRegistration: Int,
        ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        ptHoursSinceHighRiskWarningAtTestRegistration: Int,
    ): DonorModule.Contribution {
        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(ewHoursSinceHighRiskWarningAtTestRegistration)
            .setPtHoursSinceHighRiskWarningAtTestRegistration(ptHoursSinceHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setPtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultSettings.ewRiskLevelAtTestRegistration.value)
            .setPtRiskLevelAtTestRegistration(testResultSettings.ptRiskLevelAtTestRegistration.value)
            .build()

        Timber.i("Pending test result metadata:%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private fun finalTestMetadataDonation(
        registrationTime: Instant,
        testResult: CoronaTestResult,
        ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        ewHoursSinceHighRiskWarningAtTestRegistration: Int
    ): DonorModule.Contribution {
        val finalTestResultReceivedAt = testResultSettings.finalTestResultReceivedAt.value
        val hoursSinceTestRegistrationTime = if (finalTestResultReceivedAt != null) {
            Timber.i("finalTestResultReceivedAt: %s", finalTestResultReceivedAt)
            Timber.i("registrationTime: %s", registrationTime)
            Duration(registrationTime, finalTestResultReceivedAt).standardHours.toInt().also {
                Timber.i("Calculated hoursSinceTestRegistrationTime: %s", it)
            }
        } else {
            Timber.i("Default hoursSinceTestRegistrationTime")
            DEFAULT_HOURS_SINCE_TEST_REGISTRATION_TIME
        }

        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(ewHoursSinceHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultSettings.ewRiskLevelAtTestRegistration.value)
            .build()

        Timber.i("Final test result metadata:\n%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private fun CoronaTestResult.toPPATestResult(): PpaData.PPATestResult {
        return when (this) {
            CoronaTestResult.PCR_OR_RAT_PENDING -> PpaData.PPATestResult.TEST_RESULT_PENDING
            CoronaTestResult.PCR_POSITIVE -> PpaData.PPATestResult.TEST_RESULT_POSITIVE
            CoronaTestResult.PCR_NEGATIVE -> PpaData.PPATestResult.TEST_RESULT_NEGATIVE
            else -> PpaData.PPATestResult.TEST_RESULT_UNKNOWN
        }
    }

    private fun formString(testResultMetadata: PpaData.PPATestResultMetadata) =
        with(testResultMetadata) {
            """
             testResult=$testResult
             riskLevelAtTestRegistration=$riskLevelAtTestRegistration
             hoursSinceTestRegistration=$hoursSinceTestRegistration
             hoursSinceHighRiskWarningAtTestRegistration=$hoursSinceHighRiskWarningAtTestRegistration
             daysSinceMostRecentDateAtRiskLevelAtTestRegistration=$daysSinceMostRecentDateAtRiskLevelAtTestRegistration
            """.trimIndent()
        }

    data class TestResultMetadataContribution(
        val testResultMetadata: PpaData.PPATestResultMetadata,
        val onSuccessfulDonation: suspend () -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addTestResultMetadataSet(testResultMetadata)
        }

        override suspend fun finishDonation(successful: Boolean) {
            if (successful) {
                onSuccessfulDonation()
            } // else keep data for next submission
        }
    }

    object TestResultMetadataNoContribution : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) = Unit
        override suspend fun finishDonation(successful: Boolean) = Unit
    }

    companion object {
        private const val DEFAULT_HOURS_SINCE_TEST_REGISTRATION_TIME = -1
    }
}
