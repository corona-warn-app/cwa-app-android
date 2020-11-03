package de.rki.coronawarnapp.risk.result

//TODO("Adjust Types")
data class RiskResult(
    val transmissionRiskLevel: Any,
    val normalizedTime: Any,
    val riskLevel: Int
)
