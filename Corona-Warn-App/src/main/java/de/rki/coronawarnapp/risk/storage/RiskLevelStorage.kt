package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.warning.riskcalculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

interface RiskLevelStorage {

    /**
     * All currently available risk results.
     * This is an expensive operation on tester builds due to mapping all available windows.
     * Newest item first.
     */
    val allRiskLevelResults: Flow<List<RiskLevelResult>>

    /**
     * The newest 2 results.
     * Use by the risklevel detector to check for state changes (LOW/INCREASED RISK).
     * Can be 0-2 entries.
     * Newest item first.
     */
    val latestRiskLevelResults: Flow<List<RiskLevelResult>>

    /**
     * The newest result, and the last successfully result result.
     * Used by the tracing info cards in home and details screen.
     * Can be 0-2 entries.
     * Newest item first.
     */
    val latestAndLastSuccessful: Flow<List<RiskLevelResult>>

    /**
     * Risk level per date/day
     * Used by contact diary overview
     * Item with newest date first.
     */
    val aggregatedRiskPerDateResults: Flow<List<AggregatedRiskPerDateResult>>

    /**
     * Risk level per date/day and checkIn
     * Used by contact diary overview
     */
    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>>

    /**
     * Risk level per date/day aggregated over check-ins
     * Used by contact diary overview
     */
    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>>

    /**
     * Risk level per date/day aggregated form Exposure Windows and Presence Tracing
     * Used by contact diary overview
     */
    val aggregatedDayRisk: Flow<List<AggregatedDayRisk>>

    suspend fun deleteAggregatedRiskPerDateResults(results: List<AggregatedRiskPerDateResult>)

    suspend fun storeResult(result: RiskLevelResult)

    suspend fun clear()
}

data class AggregatedDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)
