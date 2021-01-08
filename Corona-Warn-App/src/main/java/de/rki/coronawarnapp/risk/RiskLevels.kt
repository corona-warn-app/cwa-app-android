package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult

interface RiskLevels {

    fun determineRisk(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindows: List<ExposureWindow>
    ): AggregatedRiskResult
}
