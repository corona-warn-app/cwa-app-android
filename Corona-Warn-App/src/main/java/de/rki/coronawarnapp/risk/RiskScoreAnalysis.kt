package de.rki.coronawarnapp.risk

interface RiskScoreAnalysis {

    fun withinDefinedLevelThreshold(
        riskScore: Double,
        min: Int,
        max: Int
    ): Boolean

}
