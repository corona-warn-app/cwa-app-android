package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass.AttenuationDuration

interface RiskLevelCalculation {

    @Deprecated("Switch to new calculation with Exposure Windows")
    fun calculateRiskScore(
        attenuationParameters: AttenuationDuration,
        exposureSummary: ExposureSummary
    ): Double
}
