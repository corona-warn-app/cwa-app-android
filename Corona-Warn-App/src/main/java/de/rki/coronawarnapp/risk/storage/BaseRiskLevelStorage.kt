package de.rki.coronawarnapp.risk.storage

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.mapToRiskState
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelTaskResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.toPersistedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber
import java.lang.reflect.Modifier.PRIVATE
import kotlin.math.max
import de.rki.coronawarnapp.util.flow.combine as flowCombine

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

    final override val allEwRiskLevelResults: Flow<List<EwRiskLevelResult>> = combine(
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

    override val latestEwRiskLevelResults: Flow<List<EwRiskLevelResult>> = riskResultsTables.latestEntries(2)
        .map { results ->
            Timber.v("Mapping latestRiskLevelResults:\n%s", results.joinToString("\n"))
            results.combineWithWindows(null)
        }
        .shareLatest(tag = TAG, scope = scope)

    override suspend fun storeResult(resultEw: EwRiskLevelResult) {
        Timber.d("Storing result (exposureWindows.size=%s)", resultEw.exposureWindows?.size)

        val storedResultId = try {
            val startTime = System.currentTimeMillis()

            require(resultEw.ewAggregatedRiskResult == null || resultEw.failureReason == null) {
                "A result needs to have either an aggregatedRiskResult or a failureReason, not both!"
            }

            val resultToPersist = resultEw.toPersistedRiskResult()
            riskResultsTables.insertEntry(resultToPersist).also {
                Timber.d("Storing RiskLevelResult took %dms.", (System.currentTimeMillis() - startTime))
            }

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

            riskResultsTables.deleteOldest(storedResultLimit).also {
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
        aggregatedRiskPerDateResultTables.allEntries()
            .map {
                it.map { persistedAggregatedRiskPerDateResult ->
                    persistedAggregatedRiskPerDateResult.toAggregatedRiskPerDateResult()
                }
            }
            .shareLatest(tag = TAG, scope = scope)
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
            combineRisk(ptRiskList, ewRiskList)
        }

    override val latestAndLastSuccessfulEwRiskLevelResult: Flow<List<EwRiskLevelResult>> = riskResultsTables
        .latestAndLastSuccessful()
        .map { results ->
            Timber.v("Mapping latestAndLastSuccessful:\n%s", results.joinToString("\n"))
            results.combineWithWindows(null)
        }
        .shareLatest(tag = TAG, scope = scope)

    private val latestAndLastSuccessfulPtRiskLevelResult: Flow<List<PtRiskLevelResult>> =
        presenceTracingRiskRepository
            .latestAndLastSuccessful()
            .shareLatest(tag = TAG, scope = scope)

    // TODO maybe refactor
    override val latestAndLastSuccessfulCombinedEwPtRiskLevelResult: Flow<List<CombinedEwPtRiskLevelResult>>
        get() = combine(
            latestAndLastSuccessfulEwRiskLevelResult,
            latestAndLastSuccessfulPtRiskLevelResult
        ) { ewRiskLevelResults, ptRiskLevelResults ->
            val latestEwResult = ewRiskLevelResults.maxByOrNull { it.calculatedAt }
            val latestPtResult = ptRiskLevelResults.maxByOrNull { it.calculatedAt }
            val combinedList = mutableListOf<CombinedEwPtRiskLevelResult>()
            if (latestEwResult != null && latestPtResult != null) {
                combinedList.add(
                    CombinedEwPtRiskLevelResult(
                        ewRiskLevelResult = latestEwResult,
                        ptRiskLevelResult = latestPtResult
                    )
                )
            }
            val lastSuccessfulEwResult = ewRiskLevelResults
                .filter { it.wasSuccessfullyCalculated }.maxByOrNull { it.calculatedAt }
            val lastSuccessfulPtResult = ptRiskLevelResults
                .filter { it.wasSuccessfullyCalculated }.maxByOrNull { it.calculatedAt }
            if (lastSuccessfulEwResult != null && lastSuccessfulPtResult != null) {
                combinedList.add(
                    CombinedEwPtRiskLevelResult(
                        ewRiskLevelResult = lastSuccessfulEwResult,
                        // current ptDayRiskStates belong to the last successful calculation - ugly
                        ptRiskLevelResult = lastSuccessfulPtResult.copy(
                            presenceTracingDayRisk = ptDayRiskStates.first()
                        )
                    )
                )
            }
            combinedList
        }

    private val latestPtRiskLevelResults: Flow<List<PtRiskLevelResult>> =
        presenceTracingRiskRepository
            .latestEntries(2)
            .shareLatest(tag = TAG, scope = scope)

    override val latestCombinedEwPtRiskLevelResults: Flow<List<CombinedEwPtRiskLevelResult>>
        get() = combine(
            latestEwRiskLevelResults,
            latestPtRiskLevelResults
        ) { ewRiskLevelResults, ptRiskLevelResults ->
            val latestEwResult = ewRiskLevelResults.maxByOrNull { it.calculatedAt }
            val latestPtResult = ptRiskLevelResults.maxByOrNull { it.calculatedAt }
            val olderEwResult = ewRiskLevelResults.maxByOrNull { it.calculatedAt }
            val olderPtResult = ptRiskLevelResults.maxByOrNull { it.calculatedAt }
            val combinedList = mutableListOf<CombinedEwPtRiskLevelResult>()
            if (latestEwResult != null && latestPtResult != null) {
                combinedList.add(
                    CombinedEwPtRiskLevelResult(
                        ewRiskLevelResult = latestEwResult,
                        ptRiskLevelResult = latestPtResult
                    )
                )
            }
            if (olderEwResult != null && olderPtResult != null) {
                combinedList.add(
                    CombinedEwPtRiskLevelResult(
                        ewRiskLevelResult = olderEwResult,
                        ptRiskLevelResult = olderPtResult
                    )
                )
            }
            combinedList
        }

    internal abstract suspend fun storeExposureWindows(storedResultId: String, resultEw: EwRiskLevelResult)

    internal abstract suspend fun deletedOrphanedExposureWindows()

    override suspend fun clear() {
        Timber.w("clear() - Clearing stored risklevel/exposure-detection results.")
        database.clearAllTables()
    }

    companion object {
        private const val TAG = "RiskLevelStorage"
    }
}

@VisibleForTesting(otherwise = PRIVATE)
internal fun combineRisk(
    ptRiskList: List<PresenceTracingDayRisk>,
    exposureWindowDayRiskList: List<ExposureWindowDayRisk>
): List<CombinedEwPtDayRisk> {
    val allDates = ptRiskList.map { it.localDateUtc }.plus(exposureWindowDayRiskList.map { it.localDateUtc }).distinct()
    return allDates.map { date ->
        val ptRisk = ptRiskList.find { it.localDateUtc == date }
        val ewRisk = exposureWindowDayRiskList.find { it.localDateUtc == date }
        CombinedEwPtDayRisk(
            date,
            max(
                ptRisk?.riskState,
                ewRisk?.riskLevel?.mapToRiskState()
            )
        )
    }
}

internal fun max(left: RiskState?, right: RiskState?): RiskState {
    return if (left == RiskState.INCREASED_RISK || right == RiskState.INCREASED_RISK) RiskState.INCREASED_RISK
    else if (left == RiskState.LOW_RISK || right == RiskState.LOW_RISK) RiskState.LOW_RISK
    else RiskState.CALCULATION_FAILED
}

internal fun max(left: Instant, right: Instant): Instant {
    return Instant.ofEpochMilli(max(left.millis, right.millis))
}

internal fun max(left: LocalDate?, right: LocalDate?): LocalDate? {
    if (left == null) return right
    if (right == null) return left
    return if (left.isAfter(right)) left
    else right
}
