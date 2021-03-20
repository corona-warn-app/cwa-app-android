package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.formatter.TestResult
import kotlinx.coroutines.flow.first
import org.joda.time.Instant
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
    suspend fun saveTestResultAnalyticsSettings(testResult: TestResult) {
        val validTestResults = listOf(
            TestResult.POSITIVE,
            TestResult.PENDING,
            TestResult.NEGATIVE
        )

        if (testResult !in validTestResults) return // Not interested in other values

        // User consented to donate analytics data
        if (analyticsSettings.analyticsEnabled.value) {
            val lastRiskResult = riskLevelStorage
                .latestAndLastSuccessful
                .first()
                .tryLatestResultsWithDefaults()
                .lastCalculated
            testResultDonorSettings.saveTestResultDonorDataAtRegistration(testResult, lastRiskResult)
        }
    }

    fun updatePendingTestResultReceivedTime(newTestResult: TestResult) {
        // Analytics is enabled
        val shouldUpdate = analyticsSettings.analyticsEnabled.value &&
            // Test was scanned after giving consent and this a QR-Code Test registration
            // For TAN test registration this flag is not set
            testResultDonorSettings.testScannedAfterConsent.value &&
            // Result was Pending
            testResultDonorSettings.testResultAtRegistration.value == TestResult.PENDING &&
            // Final Test result received
            newTestResult in listOf(TestResult.POSITIVE, TestResult.NEGATIVE)
        if (shouldUpdate) {
            Timber.d("Update finalTestResultReceivedAt time")
            // TODO clarify this
            testResultDonorSettings.finalTestResultReceivedAt.update { timeStamper.nowUTC }
            testResultDonorSettings.testResultAtRegistration.update { newTestResult }
        }
    }

    /**
     * Clear saved test donor saved metadata
     */
    fun clear() {
        testResultDonorSettings.clear()
    }
}
