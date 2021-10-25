package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
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
    ewRepository: AnalyticsTestResultEWRepository,
    timeStamper: TimeStamper,
) : AnalyticsTestResultDonor(testResultSettings, ewRepository, timeStamper) {
    override val type = CoronaTest.Type.PCR
}

@Singleton
class AnalyticsRATestResultDonor @Inject constructor(
    testResultSettings: AnalyticsRATestResultSettings,
    ewRepository: AnalyticsTestResultEWRepository,
    timeStamper: TimeStamper,
) : AnalyticsTestResultDonor(testResultSettings, ewRepository, timeStamper) {
    override val type = CoronaTest.Type.RAPID_ANTIGEN
}

abstract class AnalyticsTestResultDonor(
    private val testResultSettings: AnalyticsTestResultSettings,
    private val ewRepository: AnalyticsTestResultEWRepository,
    private val timeStamper: TimeStamper,
) : DonorModule {

    abstract val type: CoronaTest.Type

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
            testResult.isPending && isDiffHoursMoreThanConfigHoursForPendingTest ->
                pendingTestMetadataDonation(
                    hoursSinceTestRegistrationTime = hoursSinceTestRegistrationTime,
                    testResult = testResult,
                )

            /**
             * If the test result turns POSITIVE or NEGATIVE,
             * it is included in the next submission. Afterwards,
             * the collected metric data is removed.
             */
            testResult.isFinal ->
                finalTestMetadataDonation(
                    timestampAtRegistration,
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
        ewRepository.deleteAll(type)
    }

    private fun pendingTestMetadataDonation(
        hoursSinceTestRegistrationTime: Int,
        testResult: CoronaTestResult,
    ): DonorModule.Contribution {
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
            .build()

        Timber.i("Pending test result metadata:%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::deleteData)
    }

    private suspend fun finalTestMetadataDonation(
        registrationTime: Instant,
        testResult: CoronaTestResult,
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
            .setHoursSinceHighRiskWarningAtTestRegistration(
                testResultSettings.ewHoursSinceHighRiskWarningAtTestRegistration.value
            )
            .setDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                testResultSettings.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value
            )
            .setTestResult(testResult.toPPATestResult())
            .setRiskLevelAtTestRegistration(testResultSettings.ewRiskLevelAtTestRegistration.value)
            .setPtRiskLevelAtTestRegistration(testResultSettings.ptRiskLevelAtTestRegistration.value)
            .addAllExposureWindowsAtTestRegistration(ewRepository.getAll(type).asPpaData())
            .build()

        Timber.i("Final test result metadata:\n%s", formString(testResultMetaData))
        return TestResultMetadataContribution(testResultMetaData, ::deleteData)
    }

    private fun CoronaTestResult.toPPATestResult(): PpaData.PPATestResult {
        return when (this) {
            CoronaTestResult.PCR_OR_RAT_PENDING -> when (type) {
                CoronaTest.Type.PCR -> PpaData.PPATestResult.TEST_RESULT_PENDING
                CoronaTest.Type.RAPID_ANTIGEN -> PpaData.PPATestResult.TEST_RESULT_RAT_PENDING
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

    companion object {
        private const val DEFAULT_HOURS_SINCE_TEST_REGISTRATION_TIME = -1
    }
}

@VisibleForTesting
internal fun List<AnalyticsTestResultEwEntityWrapper>.asPpaData() = map {
    val scanInstances = it.scanInstanceEntities.map { scanInstance ->
        PpaData.PPAExposureWindowScanInstance.newBuilder()
            .setMinAttenuation(scanInstance.minAttenuation)
            .setTypicalAttenuation(scanInstance.typicalAttenuation)
            .setSecondsSinceLastScan(scanInstance.secondsSinceLastScan)
            .build()
    }

    val exposureWindow = PpaData.PPAExposureWindow.newBuilder()
        .setDate(it.exposureWindowEntity.dateMillis / 1000)
        .setCalibrationConfidence(it.exposureWindowEntity.calibrationConfidence)
        .setInfectiousnessValue(it.exposureWindowEntity.infectiousness)
        .setReportTypeValue(it.exposureWindowEntity.reportType)
        .addAllScanInstances(scanInstances)
        .build()

    PpaData.PPANewExposureWindow.newBuilder()
        .setExposureWindow(exposureWindow)
        .setNormalizedTime(it.exposureWindowEntity.normalizedTime)
        .setTransmissionRiskLevel(it.exposureWindowEntity.transmissionRiskLevel)
        .build()
}
