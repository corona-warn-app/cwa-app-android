package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import kotlinx.coroutines.flow.Flow

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
    val latestAndLastSuccessfulCombinedEwPtRiskLevelResult: Flow<LastCombinedRiskResults>

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

    suspend fun clearResults()
}
