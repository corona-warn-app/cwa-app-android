package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.risk.CombinedEwPtDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.ExposureWindowsFilter
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.LastSuccessfulRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import dgca.verifier.app.engine.UTC_ZONE_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import kotlinx.coroutines.flow.combine as flowCombine

abstract class BaseRiskLevelStorage constructor(
    private val riskResultDatabaseFactory: RiskResultDatabase.Factory,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val riskCombinator: RiskCombinator,
    private val ewFilter: ExposureWindowsFilter
) : RiskLevelStorage {

    private val database by lazy { riskResultDatabaseFactory.create() }
    internal val ewRiskResultsTables by lazy { database.riskResults() }
    internal val exposureWindowsTables by lazy { database.exposureWindows() }
    internal val aggregatedRiskPerDateResultTables by lazy { database.aggregatedRiskPerDate() }

    abstract val storedResultLimit: Int

    private suspend fun List<PersistedRiskLevelResultDao>.combineWithWindows(
        givenWindows: List<PersistedExposureWindowDaoWrapper>?
    ): List<EwRiskLevelTaskResult> {
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

    // only for testers build
    final override val allEwRiskLevelResultsWithExposureWindows: Flow<List<EwRiskLevelResult>> =
        ewRiskResultsTables.allEntries().map {
            it.combineWithWindows(null)
        }

    override val allEwRiskLevelResults: Flow<List<EwRiskLevelResult>> = ewRiskResultsTables.allEntries()
        .map { results ->
            results.map { it.toRiskResult() }
        }

    override suspend fun storeResult(resultEw: EwRiskLevelResult) {
        Timber.d("Storing result (exposureWindows.size=%s)", resultEw.exposureWindows?.size)

        val storedResultId = try {

            require(resultEw.ewAggregatedRiskResult == null || resultEw.failureReason == null) {
                "A result needs to have either an aggregatedRiskResult or a failureReason, not both!"
            }

            val resultToPersist = resultEw.toPersistedRiskResult()
            ewRiskResultsTables.insertEntry(resultToPersist)

            resultEw.ewAggregatedRiskResult?.exposureWindowDayRisks?.let {
                insertAggregatedRiskPerDateResults(it)
            }

            resultToPersist.id
        } catch (e: Exception) {
            Timber.e(e, "Failed to store latest result: %s", resultEw)
            throw e
        }

        try {
            Timber.d("Cleaning up old results.")

            ewRiskResultsTables.deleteOldest(storedResultLimit).also {
                Timber.d("$it old results were deleted.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to clean up old results.")
            throw e
        }

        Timber.d("Storing exposure windows.")
        storeExposureWindows(storedResultId = storedResultId, resultEw)

        Timber.d("Deleting orphaned exposure windows.")
        deletedOrphanedExposureWindows()
    }

    override val ewDayRiskStates: Flow<List<ExposureWindowDayRisk>> by lazy {
        combine(
            ewRiskResultsTables.latestEntries(1),
            aggregatedRiskPerDateResultTables.allEntries()
        ) { latestAndLastSuccessful, riskPerDate ->
            val latest = latestAndLastSuccessful.firstOrNull()
            val dayRisks = riskPerDate.map { riskPerDateResult ->
                riskPerDateResult.toExposureWindowDayRisk()
            }
            ewFilter.filterDayRisksByAge(
                dayRisks,
                latest?.calculatedAt ?: Instant.EPOCH
            )
        }
    }

    private suspend fun insertAggregatedRiskPerDateResults(
        exposureWindowDayRisks: List<ExposureWindowDayRisk>
    ) {
        Timber.d("insertAggregatedRiskPerDateResults(aggregatedRiskPerDateResults=%s)", exposureWindowDayRisks)
        try {
            aggregatedRiskPerDateResultTables.insertRisk(
                exposureWindowDayRisks.map {
                    it.toPersistedAggregatedRiskPerDateResult()
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to store risk level per date results")
        }
    }

    override suspend fun deleteAggregatedRiskPerDateResults(results: List<ExposureWindowDayRisk>) {
        Timber.d("deleteAggregatedRiskPerDateResults(results=%s)", results)
        try {
            aggregatedRiskPerDateResultTables.delete(results.map { it.toPersistedAggregatedRiskPerDateResult() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete risk level per date results")
        }
    }

    override val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        presenceTracingRiskRepository.traceLocationCheckInRiskStates

    override val ptDayRiskStates: Flow<List<PresenceTracingDayRisk>> =
        presenceTracingRiskRepository.presenceTracingDayRisk

    override val combinedEwPtDayRisk: Flow<List<CombinedEwPtDayRisk>>
        get() = flowCombine(
            ptDayRiskStates,
            ewDayRiskStates
        ) { ptRiskList, ewRiskList ->
            riskCombinator.combineRisk(ptRiskList, ewRiskList)
        }

    override val allPtRiskLevelResults: Flow<List<PtRiskLevelResult>> =
        presenceTracingRiskRepository
            .allRiskLevelResults

    // used for risk level change detector to trigger notification
    override val allCombinedEwPtRiskLevelResults: Flow<List<CombinedEwPtRiskLevelResult>>
        get() = combine(
            allEwRiskLevelResults,
            allPtRiskLevelResults
        ) { ewRiskLevelResults, ptRiskLevelResults ->
            riskCombinator.combineEwPtRiskLevelResults(ptRiskLevelResults, ewRiskLevelResults)
                .sortedByDescending { it.calculatedAt }
        }

    // used for risk state in tracing state/details
    override val latestAndLastSuccessfulCombinedEwPtRiskLevelResult: Flow<LastCombinedRiskResults>
        get() = combine(
            allCombinedEwPtRiskLevelResults,
            ewDayRiskStates,
        ) { combinedResults, ewDayRiskStates ->

            val lastCalculated = (combinedResults.firstOrNull() ?: riskCombinator.initialCombinedResult).copy(
                // need to supplement the data here as they are null by default
                exposureWindowDayRisks = ewDayRiskStates,
            )

            val lastSuccessfullyCalculated = combinedResults.find {
                it.wasSuccessfullyCalculated
            } ?: riskCombinator.initialCombinedResult

            LastCombinedRiskResults(
                lastCalculated = lastCalculated,
                lastSuccessfullyCalculatedRiskState = lastSuccessfullyCalculated.riskState
            )
        }

    override val lastSuccessfulPtRiskResult: Flow<LastSuccessfulRiskResult?>
        get() = presenceTracingRiskRepository.lastSuccessfulRiskLevelResult.map {
            it?.let {
                LastSuccessfulRiskResult(
                    riskState = it.riskState,
                    mostRecentDateAtRiskState = it.mostRecentDateAtRiskState?.atStartOfDay(UTC_ZONE_ID)?.toInstant()
                )
            }
        }

    override val lastSuccessfulEwRiskResult: Flow<LastSuccessfulRiskResult?>
        get() = ewRiskResultsTables.lastSuccessful().map {
            it.combineWithWindows(null).firstOrNull()
        }.map {
            it?.let {
                LastSuccessfulRiskResult(
                    riskState = it.riskState,
                    mostRecentDateAtRiskState = it.mostRecentDateAtRiskState
                )
            }
        }

    internal abstract suspend fun storeExposureWindows(storedResultId: String, resultEw: EwRiskLevelResult)

    internal abstract suspend fun deletedOrphanedExposureWindows()

    override suspend fun reset() {
        Timber.w("reset() - Clearing stored exposure-detection results.")
        database.clearAllTables()
        Timber.w("reset() - Clearing stored presence tracing matches and results.")
        presenceTracingRiskRepository.clearAllTables()
    }

    override suspend fun clearResults() {
        Timber.w("clearResults() - Clearing stored exposure-detection results.")
        database.clearAllTables()
        Timber.w("clearResults() - Clearing stored presence tracing results.")
        presenceTracingRiskRepository.clearResults()
    }
}
