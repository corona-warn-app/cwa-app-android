package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass.AttenuationDuration

interface RiskLevelCalculation {

    fun calculateRiskScore(
        attenuationParameters: AttenuationDuration,
        exposureSummary: ExposureSummary
    ): Double
}
