package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass.AttenuationDuration
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

interface RiskLevelCalculation {

    @Deprecated("Switch to new calculation with Exposure Window")
    fun calculateRiskScore(
        attenuationParameters: AttenuationDuration,
        exposureSummary: ExposureSummary,
    ): Double

    fun calculateRisk(
        exposureWindow: ExposureWindow,
    ): RiskResult?

    fun aggregateResults(
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>,
        riskCalculationParameters: RiskCalculationParametersOuterClass.RiskCalculationParameters
    ): AggregatedRiskResult
}
