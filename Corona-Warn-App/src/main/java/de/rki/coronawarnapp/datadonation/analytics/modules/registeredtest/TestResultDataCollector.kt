package de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.util.formatter.TestResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TestResultDataCollector @Inject constructor(
    private val analyticsSettings: AnalyticsSettings,
    private val testResultDonorSettings: TestResultDonorSettings,
    private val riskLevelStorage: RiskLevelStorage,
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

        if (analyticsSettings.analyticsEnabled.value) {
            val lastRiskResult = riskLevelStorage
                .latestAndLastSuccessful
                .first()
                .tryLatestResultsWithDefaults()
                .lastCalculated
            testResultDonorSettings.saveTestResultDonorDataAtRegistration(testResult, lastRiskResult)
        }
    }
}
