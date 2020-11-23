package de.rki.coronawarnapp.risk.storage

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.toPersistedExposureWindow
import de.rki.coronawarnapp.risk.storage.internal.toPersistedRiskResult
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRiskLevelStorage @Inject constructor(
    private val riskResultDatabaseFactory: RiskResultDatabase.Factory,
    private val riskLevelResultMigrator: RiskLevelResultMigrator
) : RiskLevelStorage {

    private val database by lazy { riskResultDatabaseFactory.create() }
    private val riskResultsTable by lazy { database.riskResults() }
    private val exposureWindowsTable by lazy { database.exposureWindows() }

    // FIXME implement
    override val exposureWindows: Flow<List<ExposureWindow>> = exposureWindowsTable.allEntries().map { windows ->
        windows.map { it.toExposureWindow() }
    }

    override val riskLevelResults: Flow<List<RiskLevelResult>> = riskResultsTable.allEntries()
        .map { latestResults ->
            latestResults.map { it.toRiskResult() }
        }
        .map { results ->
            if (results.isEmpty()) {
                riskLevelResultMigrator.getLegacyResults()
            } else {
                results
            }
        }

    override val lastRiskLevelResult: Flow<RiskLevelResult> = riskLevelResults.map { results ->
        results.maxByOrNull { it.calculatedAt } ?: INITIAL_RESULT
    }

    override suspend fun getLatestResults(limit: Int): List<RiskLevelResult> = riskLevelResults.first()
        .sortedBy { it.calculatedAt }
        .takeLast(2)

    override suspend fun storeResult(result: RiskLevelResult) {
        val resultToPersist = result.toPersistedRiskResult()
        try {
            Timber.d("Storing result: %s", result)
            riskResultsTable.insertEntry(resultToPersist)
            Timber.v("Result stored.")
        } catch (e: Exception) {
            Timber.e(e, "Failed to store latest result: %s", result)
            e.report(ExceptionCategory.INTERNAL)
        }

        try {
            Timber.d("Cleaning up old results.")
            riskResultsTable.deleteOldest(RESULT_LIMIT).also {
                Timber.d("$it old results were deleted.")
            }
            Timber.v("Old results cleaned up.")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clean up old results.")
            e.report(ExceptionCategory.INTERNAL)
        }

        try {
            val windowsToPersist = result.exposureWindows?.map {
                it.toPersistedExposureWindow(riskLevelResultId = resultToPersist.id)
            }
            // TODO save exposure windows
        } catch (e: Exception) {
            Timber.e(e, "Failed to save exposure windows")
        }
    }

    override suspend fun clear() {
        Timber.w("clear() - Clearing stored riskleve/exposure-detection results.")
        database.clearAllTables()
    }

    companion object {
        private val INITIAL_RESULT = object : RiskLevelResult {
            override val riskLevel: RiskLevel = RiskLevel.LOW_LEVEL_RISK
            override val calculatedAt: Instant = Instant.EPOCH
            override val aggregatedRiskResult: AggregatedRiskResult? = null
            override val exposureWindows: List<ExposureWindow>? = null

            override val isIncreasedRisk: Boolean = false
            override val matchedKeyCount: Int = 0
            override val daysSinceLastExposure: Int = 0
        }

        private const val RESULT_LIMIT = 10
    }
}
