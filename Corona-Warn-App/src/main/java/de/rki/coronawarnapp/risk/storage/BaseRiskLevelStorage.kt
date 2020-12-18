package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import timber.log.Timber

abstract class BaseRiskLevelStorage constructor(
    private val riskResultDatabaseFactory: RiskResultDatabase.Factory,
    private val riskLevelResultMigrator: RiskLevelResultMigrator
) : RiskLevelStorage {

    private val database by lazy { riskResultDatabaseFactory.create() }
    internal val riskResultsTables by lazy { database.riskResults() }
    internal val exposureWindowsTables by lazy { database.exposureWindows() }

    abstract val storedResultLimit: Int

    private fun List<PersistedRiskLevelResultDao>.combineWithWindows(
        windows: List<PersistedExposureWindowDaoWrapper>
    ): List<RiskLevelTaskResult> = this.map { result ->
        val matchingWindows = windows.filter { it.exposureWindowDao.riskLevelResultId == result.id }
        if (matchingWindows.isEmpty()) {
            result.toRiskResult()
        } else {
            result.toRiskResult(matchingWindows)
        }
    }

    final override val allRiskLevelResults: Flow<List<RiskLevelResult>> = combine(
        riskResultsTables.allEntries(),
        exposureWindowsTables.allEntries()
    ) { allRiskResults, allWindows ->
        Timber.v("Mapping all ${allWindows.size} windows to ${allRiskResults.size} risk results.")

        val startTime = System.currentTimeMillis()
        allRiskResults.combineWithWindows(allWindows).also {
            Timber.v("Mapping took %dms", (System.currentTimeMillis() - startTime))
        }
    }
        .map { results ->
            if (results.isEmpty()) {
                riskLevelResultMigrator.getLegacyResults()
            } else {
                results
            }
        }

    override val latestRiskLevelResults: Flow<List<RiskLevelResult>> = riskResultsTables.latestEntries(2)
        .flatMapMerge { latestResults ->
            Timber.v("Retrieving windows for latestResults: %s", latestResults)
            exposureWindowsTables
                .getWindowsForResult(latestResults.map { it.id })
                .take(1)
                .map { latestResults to it }
        }
        .map { (results: List<PersistedRiskLevelResultDao>, windows: List<PersistedExposureWindowDaoWrapper>) ->
            Timber.v("Mapping ${windows.size} windows to ${results.size} latest risk results.")
            results.combineWithWindows(windows)
        }
        .map { results ->
            if (results.isEmpty()) {
                riskLevelResultMigrator.getLegacyResults()
            } else {
                results
            }
        }

    override suspend fun storeResult(result: RiskLevelResult) {
        Timber.d("Storing result (exposureWindows.size=%s)", result.exposureWindows?.size)

        val storedResultId = try {
            val startTime = System.currentTimeMillis()

            require(result.aggregatedRiskResult == null || result.failureReason == null) {
                "A result needs to have either an aggregatedRiskResult or a failureReason, not both!"
            }

            val resultToPersist = result.toPersistedRiskResult()
            riskResultsTables.insertEntry(resultToPersist).also {
                Timber.d("Storing RiskLevelResult took %dms.", (System.currentTimeMillis() - startTime))
            }

            resultToPersist.id
        } catch (e: Exception) {
            Timber.e(e, "Failed to store latest result: %s", result)
            throw e
        }

        try {
            Timber.d("Cleaning up old results.")

            riskResultsTables.deleteOldest(storedResultLimit).also {
                Timber.d("$it old results were deleted.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to clean up old results.")
            throw e
        }

        Timber.d("Storing exposure windows.")
        storeExposureWindows(storedResultId = storedResultId, result)

        Timber.d("Deleting orphaned exposure windows.")
        deletedOrphanedExposureWindows()
    }

    internal abstract suspend fun storeExposureWindows(storedResultId: String, result: RiskLevelResult)

    internal abstract suspend fun deletedOrphanedExposureWindows()

    override suspend fun clear() {
        Timber.w("clear() - Clearing stored riskleve/exposure-detection results.")
        database.clearAllTables()
    }
}
