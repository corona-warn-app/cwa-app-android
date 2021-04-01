package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import kotlinx.coroutines.flow.Flow
import org.joda.time.Instant
import org.joda.time.LocalDate

interface RiskLevelStorage {

    /** EXPOSURE WINDOW RISK RESULT
     * All currently available risk results.
     * This is an expensive operation on tester builds due to mapping all available windows.
     * Newest item first.
     */
    val allEwRiskLevelResults: Flow<List<EwRiskLevelResult>>

    /** EXPOSURE WINDOW RISK RESULT
     * The newest 2 results.
     * Use by the risk level detector to check for state changes (LOW/INCREASED RISK),
     * collecting data for analytics and survey.
     * Can be 0-2 entries.
     * Newest item first.
     */
    val latestEwRiskLevelResults: Flow<List<EwRiskLevelResult>>

    /** COMBINED RISK RESULT
     * The newest 2 results.
     * Use by the risk level detector to check for state changes (LOW/INCREASED RISK) triggering NOTIFICATION.
     * Can be 0-2 entries.
     * Newest item first.
     */
    val latestCombinedEwPtRiskLevelResults: Flow<List<CombinedEwPtRiskLevelResult>>

    /** EXPOSURE WINDOW RISK RESULT
     * The newest result, and the last successfully result.
     * Used only for analytics
     * Can be 0-2 entries.
     * Newest item first.
     */
    val latestAndLastSuccessfulEwRiskLevelResult: Flow<List<EwRiskLevelResult>>

    /** COMBINED RISK RESULT
     * The newest result, and the last successfully result for ew and pt combined.
     * Used for TRACING info cards in HOME and DETAILS SCREEN.
     * Can be 0-2 entries.
     * Newest item first.
     */
    val latestAndLastSuccessfulCombinedEwPtRiskLevelResult: Flow<List<CombinedEwPtRiskLevelResult>>

    /** EXPOSURE WINDOW RISK RESULT
     * Risk level per date/day
     * Used by contact diary overview
     * Item with newest date first.
     */
    val ewDayRiskStates: Flow<List<ExposureWindowDayRisk>>

    /** PRESENCE TRACING RISK RESULT
     * Risk level per date/day and checkIn
     * Used by contact diary overview
     */
    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>>

    /** PRESENCE TRACING RISK RESULT
     * Risk level per date/day aggregated over check-ins
     */
    val ptDayRiskStates: Flow<List<PresenceTracingDayRisk>>

    /** COMBINED RISK RESULT
     * Risk level per date/day aggregated form Exposure Windows and Presence Tracing
     */
    val combinedEwPtDayRisk: Flow<List<CombinedEwPtDayRisk>>

    suspend fun deleteAggregatedRiskPerDateResults(results: List<ExposureWindowDayRisk>)

    suspend fun storeResult(resultEw: EwRiskLevelResult)

    suspend fun clear()
}

data class CombinedEwPtDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class CombinedEwPtRiskLevelResult(
    val ptRiskLevelResult: PtRiskLevelResult,
    val ewRiskLevelResult: EwRiskLevelResult
) {

    val riskState: RiskState = max(ptRiskLevelResult.riskState, ewRiskLevelResult.riskState)

    val wasSuccessfullyCalculated: Boolean
        get() = ewRiskLevelResult.ewAggregatedRiskResult != null &&
            ptRiskLevelResult.riskState != RiskState.CALCULATION_FAILED

    val calculatedAt: Instant = max(ewRiskLevelResult.calculatedAt, ptRiskLevelResult.calculatedAt)

    val daysWithEncounters: Int
        get() = when (riskState) {
            RiskState.INCREASED_RISK -> {
                (ewRiskLevelResult.ewAggregatedRiskResult?.numberOfDaysWithHighRisk ?: 0) +
                    ptRiskLevelResult.numberOfDaysWithHighRisk
            }
            RiskState.LOW_RISK -> {
                (ewRiskLevelResult.ewAggregatedRiskResult?.numberOfDaysWithLowRisk ?: 0) +
                    ptRiskLevelResult.numberOfDaysWithLowRisk
            }
            else -> 0
        }

    val lastRiskEncounterAt: LocalDate?
        get() = if (riskState == RiskState.INCREASED_RISK) {
            max(
                ewRiskLevelResult.ewAggregatedRiskResult?.mostRecentDateWithHighRisk?.toLocalDateUtc(),
                ptRiskLevelResult.mostRecentDateWithHighRisk
            )
        } else {
            max(
                ewRiskLevelResult.ewAggregatedRiskResult?.mostRecentDateWithLowRisk?.toLocalDateUtc(),
                ptRiskLevelResult.mostRecentDateWithLowRisk
            )
        }
}
