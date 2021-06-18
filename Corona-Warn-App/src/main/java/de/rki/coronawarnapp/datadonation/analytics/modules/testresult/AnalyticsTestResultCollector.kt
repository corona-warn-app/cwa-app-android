package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighEwRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighPtRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.common.isFinal
import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

class AnalyticsTestResultCollector @Inject constructor(
    private val analyticsSettings: AnalyticsSettings,
    private val pcrSettings: AnalyticsPCRTestResultSettings,
    private val raSettings: AnalyticsRATestResultSettings,
    private val riskLevelStorage: RiskLevelStorage,
    private val timeStamper: TimeStamper,
) {

    suspend fun reportTestRegistered(type: CoronaTest.Type) {
        if (analyticsDisabled) return

        val testRegisteredAt = timeStamper.nowUTC
        type.settings.testRegisteredAt.update { testRegisteredAt }

        val lastResult = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastCalculated

        type.settings.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update {
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                lastResult.ewRiskLevelResult.mostRecentDateAtRiskState?.toLocalDateUtc(),
                testRegisteredAt.toLocalDateUtc()
            )
        }

        type.settings.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update {
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                lastResult.ptRiskLevelResult.mostRecentDateAtRiskState,
                testRegisteredAt.toLocalDateUtc()
            )
        }

        if (lastResult.ewRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allEwRiskLevelResults
                .first()
                .getLastChangeToHighEwRiskBefore(testRegisteredAt)?.let {
                    val hours = Duration(
                        it,
                        testRegisteredAt
                    ).standardHours.toInt()
                    type.settings.ewHoursSinceHighRiskWarningAtTestRegistration.update {
                        hours
                    }
                }
        }

        if (lastResult.ptRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allPtRiskLevelResults
                .first()
                .getLastChangeToHighPtRiskBefore(testRegisteredAt)?.let {
                    val hours = Duration(
                        it,
                        testRegisteredAt
                    ).standardHours.toInt()
                    type.settings.ptHoursSinceHighRiskWarningAtTestRegistration.update {
                        hours
                    }
                }
        }
    }

    suspend fun reportTestResultAtRegistration(testResult: CoronaTestResult, type: CoronaTest.Type) {
        if (analyticsDisabled) return

        val validTestResults = when (type) {
            PCR -> listOf(
                CoronaTestResult.PCR_POSITIVE,
                CoronaTestResult.PCR_OR_RAT_PENDING,
                CoronaTestResult.PCR_NEGATIVE
            )
            RAPID_ANTIGEN -> listOf(
                CoronaTestResult.RAT_POSITIVE,
                CoronaTestResult.PCR_OR_RAT_PENDING,
                CoronaTestResult.RAT_NEGATIVE
            )
        }

        if (testResult !in validTestResults) return // Not interested in other values

        type.settings.testResultAtRegistration.update { testResult }

        if (testResult.isFinal) {
            type.settings.finalTestResultReceivedAt.update { timeStamper.nowUTC }
        }

        val lastRiskLevel = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastSuccessfullyCalculated

        type.settings.ewRiskLevelAtTestRegistration.update {
            lastRiskLevel.ewRiskLevelResult.riskState.toMetadataRiskLevel()
        }
        type.settings.ptRiskLevelAtTestRegistration.update {
            lastRiskLevel.ptRiskLevelResult.riskState.toMetadataRiskLevel()
        }
    }

    fun reportTestResultReceived(testResult: CoronaTestResult, type: CoronaTest.Type) {
        if (analyticsDisabled) return
        if (testResult.isFinal) {
            val receivedAt = timeStamper.nowUTC
            Timber.d("finalTestResultReceivedAt($testResult, $receivedAt)")
            type.settings.finalTestResultReceivedAt.update { receivedAt }
        }
    }

    /**
     * Clear saved test donor saved metadata
     */
    fun clear(type: CoronaTest.Type) {
        Timber.d("clear TestResultDonorSettings")
        type.settings.clear()
    }

    private val analyticsDisabled: Boolean
        get() = !analyticsSettings.analyticsEnabled.value

    private val CoronaTest.Type.settings: AnalyticsTestResultSettings
        get() = when (this) {
            PCR -> pcrSettings
            RAPID_ANTIGEN -> raSettings
        }
}
