package de.rki.coronawarnapp.risk

interface TraceLocationCheckInRisk {
    val checkInId: Long
    val riskState: RiskState
}
