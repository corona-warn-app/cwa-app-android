package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestEwResultsWithDefaults
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class TestResultDataCollector @Inject constructor(
    private val analyticsSettings: AnalyticsSettings,
    private val testResultDonorSettings: TestResultDonorSettings,
    private val riskLevelStorage: RiskLevelStorage,
    private val timeStamper: TimeStamper,
) {

    /**
     *  Collect Test result registration only after user has given a consent.
     *  exclude any registered test result before giving a consent
     */
    suspend fun saveTestResultAnalyticsSettings(testResult: CoronaTestResult) {
        val validTestResults = listOf(
            CoronaTestResult.PCR_POSITIVE,
            CoronaTestResult.PCR_OR_RAT_PENDING,
            CoronaTestResult.PCR_NEGATIVE
        )

        if (testResult !in validTestResults) return // Not interested in other values

        // User consented to donate analytics data
        if (analyticsSettings.analyticsEnabled.value) {
            val lastRiskResult = riskLevelStorage
                .latestAndLastSuccessfulEwRiskLevelResult
                .first()
                .tryLatestEwResultsWithDefaults()
                .lastCalculated
            Timber.d("saveTestResultDonorDataAtRegistration($testResult, $lastRiskResult)")
            testResultDonorSettings.saveTestResultDonorDataAtRegistration(testResult, lastRiskResult)
        }
    }

    fun updatePendingTestResultReceivedTime(newTestResult: CoronaTestResult) {
        // Analytics is enabled
        val shouldUpdate = analyticsSettings.analyticsEnabled.value &&
            // Test was scanned after giving consent and this a QR-Code Test registration
            // For TAN test registration this flag is not set
            testResultDonorSettings.testScannedAfterConsent.value &&
            // Result was Pending
            testResultDonorSettings.testResultAtRegistration.value == CoronaTestResult.PCR_OR_RAT_PENDING &&
            // Final Test result received
            newTestResult in listOf(CoronaTestResult.PCR_POSITIVE, CoronaTestResult.PCR_NEGATIVE)
        if (shouldUpdate) {
            val receivedAt = timeStamper.nowUTC
            Timber.d("updatePendingTestResultReceivedTime($newTestResult, $receivedAt")
            testResultDonorSettings.finalTestResultReceivedAt.update { receivedAt }
            testResultDonorSettings.testResultAtRegistration.update { newTestResult }
        }
    }

    /**
     * Clear saved test donor saved metadata
     */
    fun clear() {
        Timber.d("clear TestResultDonorSettings")
        testResultDonorSettings.clear()
    }
}
