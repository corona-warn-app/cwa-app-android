package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

abstract class BaseRiskLevelStorage constructor(
    private val riskResultDatabaseFactory: RiskResultDatabase.Factory,
    private val riskLevelResultMigrator: RiskLevelResultMigrator,
    scope: CoroutineScope
) : RiskLevelStorage {

    private val database by lazy { riskResultDatabaseFactory.create() }
    internal val riskResultsTables by lazy { database.riskResults() }
    internal val exposureWindowsTables by lazy { database.exposureWindows() }

    abstract val storedResultLimit: Int

    private suspend fun List<PersistedRiskLevelResultDao>.combineWithWindows(
        givenWindows: List<PersistedExposureWindowDaoWrapper>?
    ): List<RiskLevelTaskResult> {
        if (this.isEmpty()) return emptyList()

        val windows = if (givenWindows != null) {
            Timber.v("Using ${givenWindows.size} given windows for combining.")
            givenWindows
        } else {
            Timber.v("Retrieving windows for %d results", this.size)
            exposureWindowsTables.getWindowsForResult(this.map { it.id }).first()
        }

        Timber.v("Mapping ${windows.size} windows to ${this.size} risk results.")

        return this.map { result ->
            val matchingWindows = windows.filter { it.exposureWindowDao.riskLevelResultId == result.id }
            if (matchingWindows.isEmpty()) {
                result.toRiskResult()
            } else {
                result.toRiskResult(matchingWindows)
            }
        }
    }

    private suspend fun List<RiskLevelTaskResult>.fallbackToLegacyIfEmpty(): List<RiskLevelResult> =
        if (isNotEmpty()) {
            this
        } else {
            riskLevelResultMigrator.getLegacyResults()
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
        .map { results -> results.fallbackToLegacyIfEmpty() }
        .shareLatest(tag = TAG, scope = scope)

    override val latestRiskLevelResults: Flow<List<RiskLevelResult>> = riskResultsTables.latestEntries(2)
        .map { results ->
            Timber.v("Mapping latestRiskLevelResults:\n%s", results.joinToString("\n"))
            results.combineWithWindows(null)
        }
        .map { results -> results.fallbackToLegacyIfEmpty() }
        .shareLatest(tag = TAG, scope = scope)

    override val latestAndLastSuccessful: Flow<List<RiskLevelResult>> = riskResultsTables.latestAndLastSuccessful()
        .map { results ->
            Timber.v("Mapping latestAndLastSuccessful:\n%s", results.joinToString("\n"))
            results.combineWithWindows(null)
        }
        .map { results -> results.fallbackToLegacyIfEmpty() }
        .shareLatest(tag = TAG, scope = scope)

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

    companion object {
        private const val TAG = "RiskLevelStorage"
    }
}
