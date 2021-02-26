package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult

interface RiskLevels {

    fun calculateRisk(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindow: ExposureWindow
    ): RiskResult?

    fun aggregateResults(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindowResultMap: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskResult
}
