package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.isFinalResult
import de.rki.coronawarnapp.coronatest.server.isPending
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindow
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import java.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRTestResultDonor @Inject constructor(
    testResultSettings: AnalyticsPCRTestResultSettings,
    timeStamper: TimeStamper,
) : AnalyticsTestResultDonor(testResultSettings, timeStamper) {
    override val type = BaseCoronaTest.Type.PCR
}

@Singleton
class AnalyticsRATestResultDonor @Inject constructor(
    testResultSettings: AnalyticsRATestResultSettings,
    timeStamper: TimeStamper,
) : AnalyticsTestResultDonor(testResultSettings, timeStamper) {
    override val type = BaseCoronaTest.Type.RAPID_ANTIGEN
}

abstract class AnalyticsTestResultDonor(
    private val testResultSettings: AnalyticsTestResultSettings,
    private val timeStamper: TimeStamper,
) : DonorModule {

    abstract val type: BaseCoronaTest.Type

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val timestampAtRegistration = testResultSettings.testRegisteredAt.value
        if (timestampAtRegistration == null) {
            Timber.d("Skipping TestResultMetadata donation (timestampAtRegistration is missing)")
            return TestResultMetadataNoContribution
        }

        val testResult = testResultSettings.testResult.value
        if (testResult == null) {
            Timber.d("Skipping TestResultMetadata donation (testResultAtRegistration is missing)")
            return TestResultMetadataNoContribution
        }

        val hoursSinceTestRegistrationTime = Duration.between(
            timestampAtRegistration,
            testResultSettings.finalTestResultReceivedAt.value ?: timeStamper.nowUTC
        ).toHours().toInt()

        val configHours = request.currentConfig.analytics.hoursSinceTestRegistrationToSubmitTestResultMetadata
        val isDiffHoursMoreThanConfigHoursForPendingTest = hoursSinceTestRegistrationTime >= configHours
        Timber.i("hoursSinceTestRegistrationTime=$hoursSinceTestRegistrationTime, configHours=$configHours")

        return when {

            /**
             * If the test result turns POSITIVE or NEGATIVE,
             * it is included in the next submission. Afterwards,
             * the collected metric data is removed.
             */
            testResult.isFinalResult -> {
                createDonation(
                    hoursSinceTestRegistrationTime,
                    testResult,
                )
            }

            /**
             * If test is pending and
             * More than <hoursSinceTestRegistration> hours have passed since the test was registered,
             * it is included in the next submission and removed afterwards.
             * That means if the test result turns POS or NEG afterwards, this will not be submitted again
             */
            testResult.isPending && isDiffHoursMoreThanConfigHoursForPendingTest ->
                createDonation(
                    hoursSinceTestRegistrationTime,
                    testResult,
                )

            else -> {
                Timber.d("Skipping Data donation")
                TestResultMetadataNoContribution
            }
        }
    }

    override suspend fun deleteData() {
        Timber.d("Cleaning data")
        testResultSettings.clear()
    }

    private fun createDonation(
        hoursSinceTestRegistrationTime: Int,
        testResult: CoronaTestResult,
    ): DonorModule.Contribution {

        val exposureWindowsAtTestRegistration =
            testResultSettings.exposureWindowsAtTestRegistration.value?.asPpaData() ?: emptyList()

        val exposureWindowsUntilTestResult =
            testResultSettings.exposureWindowsUntilTestResult.value?.asPpaData() ?: emptyList()

        val testResultMetaData = PpaData.PPATestResultMetadata.newBuilder()
            .setHoursSinceTestRegistration(hoursSinceTestRegistrationTime)
            .setHoursSinceHighRiskWarningAtTestRegistration(
                testResultSettings.ewHoursSinceHighRiskWarningAtTestRegistration.value
            )
            .setPtHoursSinceHighRiskWarningAtTestRegistration(
                testResultSettings.ptHoursSinceHighRiskWarningAtTestRegistration.value
            )
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                testResultSettings.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value
            )
            .setPtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                testResultSettings.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultSettings.ewRiskLevelAtTestRegistration.value)
            .setPtRiskLevelAtTestRegistration(testResultSettings.ptRiskLevelAtTestRegistration.value)
            .addAllExposureWindowsAtTestRegistration(exposureWindowsAtTestRegistration)
            .addAllExposureWindowsUntilTestResult(exposureWindowsUntilTestResult)
            .build()

        Timber.i("Test result metadata:%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::deleteData)
    }

    private fun CoronaTestResult.toPPATestResult(): PpaData.PPATestResult {
        return when (this) {
            CoronaTestResult.PCR_OR_RAT_PENDING -> when (type) {
                BaseCoronaTest.Type.PCR -> PpaData.PPATestResult.TEST_RESULT_PENDING
                BaseCoronaTest.Type.RAPID_ANTIGEN -> PpaData.PPATestResult.TEST_RESULT_RAT_PENDING
            }
            CoronaTestResult.PCR_POSITIVE -> PpaData.PPATestResult.TEST_RESULT_POSITIVE
            CoronaTestResult.PCR_NEGATIVE -> PpaData.PPATestResult.TEST_RESULT_NEGATIVE
            CoronaTestResult.RAT_NEGATIVE -> PpaData.PPATestResult.TEST_RESULT_RAT_NEGATIVE
            CoronaTestResult.RAT_POSITIVE -> PpaData.PPATestResult.TEST_RESULT_RAT_POSITIVE
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
}

@VisibleForTesting
internal fun List<AnalyticsExposureWindow>.asPpaData() = map {
    val scanInstances = it.analyticsScanInstances.map { scanInstance ->
        PpaData.PPAExposureWindowScanInstance.newBuilder()
            .setMinAttenuation(scanInstance.minAttenuation)
            .setTypicalAttenuation(scanInstance.typicalAttenuation)
            .setSecondsSinceLastScan(scanInstance.secondsSinceLastScan)
            .build()
    }

    val exposureWindow = PpaData.PPAExposureWindow.newBuilder()
        .setDate(it.dateMillis / 1000)
        .setCalibrationConfidence(it.calibrationConfidence)
        .setInfectiousnessValue(it.infectiousness)
        .setReportTypeValue(it.reportType)
        .addAllScanInstances(scanInstances)
        .build()

    PpaData.PPANewExposureWindow.newBuilder()
        .setExposureWindow(exposureWindow)
        .setNormalizedTime(it.normalizedTime)
        .setTransmissionRiskLevel(it.transmissionRiskLevel)
        .build()
}
