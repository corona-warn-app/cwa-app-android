package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import kotlinx.coroutines.flow.Flow

interface RiskLevelStorage {

    val allRiskLevelResults: Flow<List<RiskLevelResult>>

    /**
     * The newest 2 results
     */
    val latestRiskLevelResults: Flow<List<RiskLevelResult>>

    suspend fun storeResult(result: RiskLevelResult)

    suspend fun clear()
}
