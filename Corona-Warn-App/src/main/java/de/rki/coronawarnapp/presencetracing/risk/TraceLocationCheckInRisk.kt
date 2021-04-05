package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.risk.RiskState
import org.joda.time.LocalDate

interface TraceLocationCheckInRisk {
    val checkInId: Long
    val localDateUtc: LocalDate
    val riskState: RiskState
}
