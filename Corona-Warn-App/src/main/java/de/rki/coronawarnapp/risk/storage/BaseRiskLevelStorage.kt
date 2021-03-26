package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.warning.riskcalculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.warning.riskcalculation.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.warning.riskcalculation.mapToRiskState
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

abstract class BaseRiskLevelStorage constructor(
    private val riskResultDatabaseFactory: RiskResultDatabase.Factory,
    presenceTracingRiskRepository: PresenceTracingRiskRepository,
    scope: CoroutineScope
) : RiskLevelStorage {

    private val database by lazy { riskResultDatabaseFactory.create() }
    internal val riskResultsTables by lazy { database.riskResults() }
    internal val exposureWindowsTables by lazy { database.exposureWindows() }
    internal val aggregatedRiskPerDateResultTables by lazy { database.aggregatedRiskPerDate() }

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
        .shareLatest(tag = TAG, scope = scope)

    override val latestRiskLevelResults: Flow<List<RiskLevelResult>> = riskResultsTables.latestEntries(2)
        .map { results ->
            Timber.v("Mapping latestRiskLevelResults:\n%s", results.joinToString("\n"))
            results.combineWithWindows(null)
        }
        .shareLatest(tag = TAG, scope = scope)

    override val latestAndLastSuccessful: Flow<List<RiskLevelResult>> = riskResultsTables.latestAndLastSuccessful()
        .map { results ->
            Timber.v("Mapping latestAndLastSuccessful:\n%s", results.joinToString("\n"))
            results.combineWithWindows(null)
        }
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

            result.aggregatedRiskResult?.aggregatedRiskPerDateResults?.let {
                insertAggregatedRiskPerDateResults(it)
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

    override val aggregatedRiskPerDateResults: Flow<List<AggregatedRiskPerDateResult>> by lazy {
        aggregatedRiskPerDateResultTables.allEntries()
            .map {
                it.map { persistedAggregatedRiskPerDateResult ->
                    persistedAggregatedRiskPerDateResult.toAggregatedRiskPerDateResult()
                }
            }
            .shareLatest(tag = TAG, scope = scope)
    }

    private suspend fun insertAggregatedRiskPerDateResults(
        aggregatedRiskPerDateResults: List<AggregatedRiskPerDateResult>
    ) {
        Timber.d("insertAggregatedRiskPerDateResults(aggregatedRiskPerDateResults=%s)", aggregatedRiskPerDateResults)
        try {
            aggregatedRiskPerDateResultTables.insertRisk(
                aggregatedRiskPerDateResults.map {
                    it.toPersistedAggregatedRiskPerDateResult()
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to store risk level per date results")
        }
    }

    override suspend fun deleteAggregatedRiskPerDateResults(results: List<AggregatedRiskPerDateResult>) {
        Timber.d("deleteAggregatedRiskPerDateResults(results=%s)", results)
        try {
            aggregatedRiskPerDateResultTables.delete(results.map { it.toPersistedAggregatedRiskPerDateResult() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete risk level per date results")
        }
    }

    override val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        presenceTracingRiskRepository.traceLocationCheckInRiskStates

    override val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        presenceTracingRiskRepository.presenceTracingDayRisk

    override val aggregatedDayRisk: Flow<List<AggregatedDayRisk>>
        get() = de.rki.coronawarnapp.util.flow.combine(
            presenceTracingDayRisk,
            aggregatedRiskPerDateResults
        ) { ptRiskList, ewRiskList ->
            combineRisk(ptRiskList, ewRiskList)
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

private fun combineRisk(
    ptRiskList: List<PresenceTracingDayRisk>,
    ewRiskList: List<AggregatedRiskPerDateResult>
): List<AggregatedDayRisk> {
    val allDates = ptRiskList.map { it.localDate }.plus(ewRiskList.map { it.day }).distinct()
    return allDates.map { date ->
        val ptRisk = ptRiskList.find { it.localDate == date }
        val ewRisk = ewRiskList.find { it.day == date }
        AggregatedDayRisk(
            date,
            max(
                ptRisk?.riskState,
                ewRisk?.riskLevel?.mapToRiskState()
            )
        )
    }
}

private fun max(left: RiskState?, right: RiskState?): RiskState {
    return if (left == RiskState.INCREASED_RISK || right == RiskState.INCREASED_RISK) RiskState.INCREASED_RISK
    else if (left == RiskState.LOW_RISK || right == RiskState.LOW_RISK) RiskState.LOW_RISK
    else RiskState.CALCULATION_FAILED
}
