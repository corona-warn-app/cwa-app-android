package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.formatter.TestResult
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestResultDonor @Inject constructor(
    private val testResultDonorSettings: TestResultDonorSettings,
    private val riskLevelSettings: RiskLevelSettings,
    private val riskLevelStorage: RiskLevelStorage,
    private val timeStamper: TimeStamper,
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val scannedAfterConsent = testResultDonorSettings.testScannedAfterConsent.value
        if (!scannedAfterConsent) {
            Timber.d("Skipping TestResultMetadata donation (testScannedAfterConsent=%s)", scannedAfterConsent)
            return TestResultMetadataNoContribution
        }

        val timestampAtRegistration = LocalData.initialTestResultReceivedTimestamp()

        if (timestampAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation timestampAtRegistration isn't found")
            return TestResultMetadataNoContribution
        }

        val configHours = request
            .currentConfig
            .analytics
            .hoursSinceTestRegistrationToSubmitTestResultMetadata

        val registrationTime = Instant.ofEpochMilli(timestampAtRegistration)
        val hoursSinceTestRegistrationTime = Duration(registrationTime, timeStamper.nowUTC).standardHours.toInt()
        val isDiffHoursMoreThanConfigHoursForPendingTest = hoursSinceTestRegistrationTime >= configHours

        val testResultAtRegistration =
            testResultDonorSettings.testResultAtRegistration.value ?: return TestResultMetadataNoContribution

        val daysSinceMostRecentDateAtRiskLevelAtTestRegistration =
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                riskLevelSettings.lastChangeCheckedRiskLevelTimestamp,
                registrationTime
            )

        val riskLevelAtRegistration = testResultDonorSettings.riskLevelAtTestRegistration.value

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
            isDiffHoursMoreThanConfigHoursForPendingTest && testResultAtRegistration.isPending ->
                pendingTestMetadataDonation(
                    hoursSinceTestRegistrationTime,
                    testResultAtRegistration,
                    daysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    hoursSinceHighRiskWarningAtTestRegistration
                )

            /**
             * If the test result turns POSITIVE or NEGATIVE,
             * it is included in the next submission. Afterwards,
             * the collected metric data is removed.
             */
            testResultAtRegistration.isFinal ->
                finalTestMetadataDonation(
                    registrationTime,
                    testResultAtRegistration,
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
        testResultDonorSettings.clear()
    }

    private fun pendingTestMetadataDonation(
        hoursSinceTestRegistrationTime: Int,
        testResult: TestResult,
        daysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        hoursSinceHighRiskWarningAtTestRegistration: Int
    ): DonorModule.Contribution {
        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(hoursSinceHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultDonorSettings.riskLevelAtTestRegistration.value)
            .build()

        Timber.i("Pending test result metadata:%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private fun finalTestMetadataDonation(
        registrationTime: Instant,
        testResult: TestResult,
        daysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int,
        hoursSinceHighRiskWarningAtTestRegistration: Int
    ): DonorModule.Contribution {
        val finalTestResultReceivedAt = testResultDonorSettings.finalTestResultReceivedAt.value
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
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultDonorSettings.riskLevelAtTestRegistration.value)
            .build()

        Timber.i("Final test result metadata:\n%s", formString(testResultMetaData))
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

    private inline val TestResult.isFinal: Boolean get() = this in listOf(TestResult.POSITIVE, TestResult.NEGATIVE)
    private inline val TestResult.isPending get() = this == TestResult.PENDING

    private fun TestResult.toPPATestResult(): PpaData.PPATestResult {
        return when (this) {
            TestResult.PENDING -> PpaData.PPATestResult.TEST_RESULT_PENDING
            TestResult.POSITIVE -> PpaData.PPATestResult.TEST_RESULT_POSITIVE
            TestResult.NEGATIVE -> PpaData.PPATestResult.TEST_RESULT_NEGATIVE
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
        val onFinishDonation: suspend () -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addTestResultMetadataSet(testResultMetadata)
        }

        override suspend fun finishDonation(successful: Boolean) {
            if (successful) {
                onFinishDonation()
            } // else Keep data for next submission
        }
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
