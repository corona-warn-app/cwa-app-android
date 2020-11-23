package de.rki.coronawarnapp.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevelResult
import kotlinx.coroutines.flow.Flow

interface RiskLevelStorage {

    val exposureWindows: Flow<List<ExposureWindow>>

    val riskLevelResults: Flow<List<RiskLevelResult>>

    val lastRiskLevelResult: Flow<RiskLevelResult>

    suspend fun getLatestResults(limit: Int): List<RiskLevelResult>

    suspend fun storeResult(result: RiskLevelResult)

    suspend fun clear()
}
