package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import kotlinx.coroutines.flow.Flow

interface RiskLevelStorage {

    val riskLevelResults: Flow<List<RiskLevelResult>>

    suspend fun storeResult(result: RiskLevelResult)

    suspend fun clear()
}
