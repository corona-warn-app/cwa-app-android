package de.rki.coronawarnapp.risk

import org.joda.time.LocalDate

interface TraceLocationCheckInRisk {
    val checkInId: Long
    val localDate: LocalDate
    val riskState: RiskState
}
