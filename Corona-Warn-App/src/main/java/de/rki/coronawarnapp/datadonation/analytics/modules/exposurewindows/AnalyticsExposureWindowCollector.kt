package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsExposureWindow
import de.rki.coronawarnapp.datadonation.analytics.common.AnalyticsScanInstance
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.util.debug.measureTime
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class AnalyticsExposureWindowCollector @Inject constructor(
    private val analyticsExposureWindowRepository: AnalyticsExposureWindowRepository,
    private val analyticsSettings: AnalyticsSettings
) {
    suspend fun reportRiskResultsPerWindow(riskResultsPerWindow: Map<ExposureWindow, RiskResult>) {
        if (analyticsSettings.analyticsEnabled.first()) {
            collectAnalyticsData(riskResultsPerWindow)
        }
    }

    private suspend fun collectAnalyticsData(riskResultsPerWindow: Map<ExposureWindow, RiskResult>) {
        measureTime(onMeasured = { Timber.d("Time per db insert of exposure window is $it") }) {
            riskResultsPerWindow.forEach {
                val analyticsExposureWindow = createAnalyticsExposureWindow(
                    it.key,
                    it.value
                )
                analyticsExposureWindowRepository.addNew(analyticsExposureWindow)
            }
        }
    }
}

private fun createAnalyticsExposureWindow(
    window: ExposureWindow,
    result: RiskResult
) = AnalyticsExposureWindow(
    calibrationConfidence = window.calibrationConfidence,
    dateMillis = window.dateMillisSinceEpoch,
    infectiousness = window.infectiousness,
    reportType = window.reportType,
    analyticsScanInstances = window.scanInstances.map { it.toAnalyticsScanInstance() },
    normalizedTime = result.normalizedTime,
    transmissionRiskLevel = result.transmissionRiskLevel
)

private fun ScanInstance.toAnalyticsScanInstance() = AnalyticsScanInstance(
    minAttenuation = minAttenuationDb,
    typicalAttenuation = typicalAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan
)
