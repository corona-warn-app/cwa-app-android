package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestResultDonor @Inject constructor(
    private val testResultDonorSettings: TestResultDonorSettings,
    private val timeStamper: TimeStamper,
    private val coronaTestRepository: CoronaTestRepository,
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val scannedAfterConsent = testResultDonorSettings.testScannedAfterConsent.value
        if (!scannedAfterConsent) {
            Timber.d("Skipping TestResultMetadata donation (scannedAfterConsent=%s)", scannedAfterConsent)
            return TestResultMetadataNoContribution
        }

        val timestampAtRegistration = coronaTestRepository.latestPCRT.first()?.registeredAt
        if (timestampAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation (timestampAtRegistration is missing)")
            return TestResultMetadataNoContribution
        }

        val testResultAtRegistration = testResultDonorSettings.testResultAtRegistration.value
        if (testResultAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation (testResultAtRegistration is missing)")
            return TestResultMetadataNoContribution
        }

        val ewLastChangeCheckedRiskLevelTimestamp = testResultDonorSettings.ewMostRecentDateWithHighOrLowRiskLevel.value

        val ptLastChangeCheckedRiskLevelTimestamp = testResultDonorSettings.ptMostRecentDateWithHighOrLowRiskLevel.value

        // Default -1 value is covered by calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
        // In case lastChangeCheckedRiskLevelTimestamp is null
        val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration =
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                ewLastChangeCheckedRiskLevelTimestamp,
                timestampAtRegistration
            )

        val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration =
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                ptLastChangeCheckedRiskLevelTimestamp,
                timestampAtRegistration
            )

        Timber.i(
            "ewLastChangeCheckedRiskLevelTimestamp=%s,timestampAtRegistration=%s",
            ewLastChangeCheckedRiskLevelTimestamp,
            timestampAtRegistration
        )

        Timber.i(
            "ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: %s",
            ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
        )

        Timber.i(
            "ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: %s",
            ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
        )

        val ewRiskLevelAtRegistration = testResultDonorSettings.ewRiskLevelAtTestRegistration.value
        val ewHighRiskResultCalculatedAt = testResultDonorSettings.riskLevelTurnedRedTime.value

        val hoursSinceEwHighRiskWarningAtTestRegistration =
            if (ewRiskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_LOW) {
                DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING
            } else {
                if (ewHighRiskResultCalculatedAt == null) {
                    Timber.d("Skipping TestResultMetadata donation (ewHighRiskResultCalculatedAt is missing)")
                    return TestResultMetadataNoContribution
                }

                Timber.i(
                    "highRiskResultCalculatedAt: %s, timestampAtRegistration: %s",
                    ewHighRiskResultCalculatedAt,
                    timestampAtRegistration
                )
                calculatedHoursSinceHighRiskWarning(ewHighRiskResultCalculatedAt, timestampAtRegistration)
            }
        Timber.i(
            "hoursSinceHighRiskWarningAtTestRegistration: %s",
            hoursSinceEwHighRiskWarningAtTestRegistration
        )

        val ptRiskLevelAtRegistration = testResultDonorSettings.ptRiskLevelAtTestRegistration.value
        val ptHighRiskResultCalculatedAt = testResultDonorSettings.riskLevelTurnedRedTime.value

        val hoursSincePtHighRiskWarningAtTestRegistration =
            if (ptRiskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_LOW) {
                DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING
            } else {
                if (ptHighRiskResultCalculatedAt == null) {
                    Timber.d("Skipping TestResultMetadata donation (ptHighRiskResultCalculatedAt is missing)")
                    return TestResultMetadataNoContribution
                }

                Timber.i(
                    "highRiskResultCalculatedAt: %s, timestampAtRegistration: %s",
                    ptHighRiskResultCalculatedAt,
                    timestampAtRegistration
                )
                calculatedHoursSinceHighRiskWarning(ptHighRiskResultCalculatedAt, timestampAtRegistration)
            }
        Timber.i(
            "hoursSincePtHighRiskWarningAtTestRegistration: %s",
            hoursSincePtHighRiskWarningAtTestRegistration
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
            isDiffHoursMoreThanConfigHoursForPendingTest && testResultAtRegistration.isPending ->
                pendingTestMetadataDonation(
                    hoursSinceTestRegistrationTime,
                    testResultAtRegistration,
                    ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    hoursSinceEwHighRiskWarningAtTestRegistration,
                    hoursSincePtHighRiskWarningAtTestRegistration,
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
                    ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration,
                    hoursSinceEwHighRiskWarningAtTestRegistration,
                    hoursSincePtHighRiskWarningAtTestRegistration,
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
        testResult: CoronaTestResult,
        daysSinceMostRecentDateAtEwRiskLevelAtTestRegistration: Int,
        daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration: Int,
        hoursSinceEwHighRiskWarningAtTestRegistration: Int,
        hoursSincePtHighRiskWarningAtTestRegistration: Int,
    ): DonorModule.Contribution {
        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(hoursSinceEwHighRiskWarningAtTestRegistration)
            .setPtHoursSinceHighRiskWarningAtTestRegistration(hoursSincePtHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtEwRiskLevelAtTestRegistration
            )
            .setPtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultDonorSettings.ewRiskLevelAtTestRegistration.value)
            .setPtRiskLevelAtTestRegistration(testResultDonorSettings.ptRiskLevelAtTestRegistration.value)
            .build()

        Timber.i("Pending test result metadata:%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private fun finalTestMetadataDonation(
        registrationTime: Instant,
        testResult: CoronaTestResult,
        daysSinceMostRecentDateAtEwRiskLevelAtTestRegistration: Int,
        daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration: Int,
        hoursSinceEwHighRiskWarningAtTestRegistration: Int,
        hoursSincePtHighRiskWarningAtTestRegistration: Int,
    ): DonorModule.Contribution {
        val finalTestResultReceivedAt = testResultDonorSettings.finalTestResultReceivedAt.value
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
            .setHoursSinceHighRiskWarningAtTestRegistration(hoursSinceEwHighRiskWarningAtTestRegistration)
            .setPtHoursSinceHighRiskWarningAtTestRegistration(hoursSincePtHighRiskWarningAtTestRegistration)
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtEwRiskLevelAtTestRegistration
            )
            .setPtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                daysSinceMostRecentDateAtPtRiskLevelAtTestRegistration
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultDonorSettings.ewRiskLevelAtTestRegistration.value)
            .setPtRiskLevelAtTestRegistration(testResultDonorSettings.ptRiskLevelAtTestRegistration.value)
            .build()

        Timber.i("Final test result metadata:\n%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::cleanUp)
    }

    private fun calculatedHoursSinceHighRiskWarning(
        highRiskResultCalculatedAt: Instant,
        registrationTime: Instant
    ): Int {
        return Duration(
            highRiskResultCalculatedAt,
            registrationTime
        ).standardHours.toInt()
    }

    private inline val CoronaTestResult.isFinal: Boolean
        get() = this in listOf(
            CoronaTestResult.PCR_POSITIVE,
            CoronaTestResult.PCR_NEGATIVE
        )
    private inline val CoronaTestResult.isPending get() = this == CoronaTestResult.PCR_OR_RAT_PENDING

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
