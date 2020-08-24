package de.rki.coronawarnapp.risk

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskScoreAnalysisImpl @Inject constructor() : RiskScoreAnalysis {

    override fun withinDefinedLevelThreshold(riskScore: Double, min: Int, max: Int) =
        riskScore >= min && riskScore <= max
}
