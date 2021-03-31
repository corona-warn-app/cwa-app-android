package de.rki.coronawarnapp.risk

import org.joda.time.LocalDate

interface TraceLocationCheckInRisk {
    val checkInId: Long
    val localDateUtc: LocalDate
    val riskState: RiskState
}
