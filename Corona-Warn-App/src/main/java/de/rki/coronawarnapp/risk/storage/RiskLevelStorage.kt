package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import kotlinx.coroutines.flow.Flow

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

    suspend fun deleteAggregatedRiskPerDateResults(results: List<AggregatedRiskPerDateResult>)

    suspend fun storeResult(result: RiskLevelResult)

    suspend fun clear()
}
