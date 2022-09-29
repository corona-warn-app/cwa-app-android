package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.isFinalResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighEwRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighPtRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsScanInstance
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.first
import java.time.Duration
import timber.log.Timber
import javax.inject.Inject

class AnalyticsTestResultCollector @Inject constructor(
    private val analyticsSettings: AnalyticsSettings,
    private val pcrSettings: AnalyticsPCRTestResultSettings,
    private val raSettings: AnalyticsRATestResultSettings,
    private val riskLevelStorage: RiskLevelStorage,
    private val timeStamper: TimeStamper,
    private val exposureWindowsSettings: AnalyticsExposureWindowsSettings
) {

    fun reportRiskResultsPerWindow(riskResultsPerWindow: Map<ExposureWindow, RiskResult>) {
        val exposureWindows = riskResultsPerWindow.map {
            it.key.toModel(it.value)
        }
        exposureWindowsSettings.currentExposureWindows.update {
            exposureWindows
        }
    }

    suspend fun reportTestRegistered(type: BaseCoronaTest.Type) {
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
                    val hours = Duration.between(
                        it,
                        testRegisteredAt
                    ).toHours().toInt()
                    type.settings.ewHoursSinceHighRiskWarningAtTestRegistration.update {
                        hours
                    }
                }
        }

        if (lastResult.ptRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allPtRiskLevelResults
                .first()
                .getLastChangeToHighPtRiskBefore(testRegisteredAt)?.let {
                    val hours = Duration.between(
                        it,
                        testRegisteredAt
                    ).toHours().toInt()
                    type.settings.ptHoursSinceHighRiskWarningAtTestRegistration.update {
                        hours
                    }
                }
        }

        type.settings.ewRiskLevelAtTestRegistration.update {
            lastResult.ewRiskLevelResult.riskState.toMetadataRiskLevel()
        }
        type.settings.ptRiskLevelAtTestRegistration.update {
            lastResult.ptRiskLevelResult.riskState.toMetadataRiskLevel()
        }

        type.settings.exposureWindowsAtTestRegistration.update {
            exposureWindowsSettings.currentExposureWindows.value
        }
    }

    fun reportTestResultReceived(testResult: CoronaTestResult, type: BaseCoronaTest.Type) {
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

        type.settings.testResult.update { testResult }

        if (testResult.isFinalResult && type.settings.finalTestResultReceivedAt.value == null) {
            type.settings.finalTestResultReceivedAt.update { timeStamper.nowUTC }

            val newExposureWindows = exposureWindowsSettings.currentExposureWindows.value?.filterExposureWindows(
                type.settings.exposureWindowsAtTestRegistration.value
            ) ?: emptyList()

            type.settings.exposureWindowsUntilTestResult.update {
                newExposureWindows
            }
        }
    }

    /**
     * Clear saved test donor saved metadata
     */
    fun clear(type: BaseCoronaTest.Type) {
        Timber.d("clear TestResultDonorSettings")
        type.settings.clear()
    }

    private val analyticsDisabled: Boolean
        get() = !analyticsSettings.analyticsEnabled.value

    private val BaseCoronaTest.Type.settings: AnalyticsTestResultSettings
        get() = when (this) {
            PCR -> pcrSettings
            RAPID_ANTIGEN -> raSettings
        }
}

private fun ExposureWindow.toModel(result: RiskResult) = AnalyticsExposureWindow(
    calibrationConfidence = calibrationConfidence,
    dateMillis = dateMillisSinceEpoch,
    infectiousness = infectiousness,
    reportType = reportType,
    normalizedTime = result.normalizedTime,
    transmissionRiskLevel = result.transmissionRiskLevel,
    analyticsScanInstances = scanInstances.map { it.toModel() }
)

private fun ScanInstance.toModel() = AnalyticsScanInstance(
    minAttenuation = minAttenuationDb,
    typicalAttenuation = typicalAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan
)

@VisibleForTesting
internal fun List<AnalyticsExposureWindow>.filterExposureWindows(
    reportedEw: List<AnalyticsExposureWindow>?
): List<AnalyticsExposureWindow> {
    val reportedEwHashs = reportedEw?.map {
        it.sha256Hash()
    } ?: return this

    return filter {
        !reportedEwHashs.contains(it.sha256Hash())
    }
}
