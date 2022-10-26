package de.rki.coronawarnapp.presencetracing.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.CheckInsFilter
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInNormalizedTime
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskRepository @Inject constructor(
    private val ptRiskCalculator: PresenceTracingRiskCalculator,
    private val databaseFactory: PresenceTracingRiskDatabase.Factory,
    private val timeStamper: TimeStamper,
    private val checkInsFilter: CheckInsFilter,
) {

    private val database by lazy {
        databaseFactory.create()
    }

    private val traceTimeIntervalMatchDao by lazy {
        database.traceTimeIntervalMatchDao()
    }

    private val riskLevelResultDao by lazy {
        database.presenceTracingRiskLevelResultDao()
    }

    val allCheckInWarningOverlaps = traceTimeIntervalMatchDao.allMatches().map { entities ->
        entities.map { it.toCheckInWarningOverlap() }
    }

    val allRiskLevelResults: Flow<List<PtRiskLevelResult>> = combine(
        riskLevelResultDao.allEntries(),
        allCheckInWarningOverlaps,
    ) { resultList, overlaps ->
        resultList.sortedByDescending {
            it.calculatedAtMillis
        }.mapIndexed { index, entity ->
            if (index == 0) {
                entity.complement(overlaps)
            } else entity.toRiskLevelResult()
        }
    }

    private suspend fun PresenceTracingRiskLevelResultEntity.complement(
        overlaps: List<CheckInWarningOverlap>
    ): PtRiskLevelResult {
        val relevantWarnings = checkInsFilter.filterCheckInWarningsByAge(
            overlaps,
            Instant.ofEpochMilli(calculatedFromMillis)
        )
        val normalizedTime = relevantWarnings.calculateNormalizedTime()

        return toRiskLevelResult(
            presenceTracingDayRisks = ptRiskCalculator.calculateDayRisk(normalizedTime),
            traceLocationCheckInRiskStates = ptRiskCalculator.calculateCheckInRiskPerDay(normalizedTime),
            checkInWarningOverlaps = relevantWarnings,
        )
    }

    val latestRiskLevelResult: Flow<PtRiskLevelResult?> = allRiskLevelResults.map {
        it.firstOrNull()
    }

    val lastSuccessfulRiskLevelResult: Flow<PtRiskLevelResult?> = allRiskLevelResults.map {
        it.firstOrNull {
            it.wasSuccessfullyCalculated
        }
    }

    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        latestRiskLevelResult.map {
            it?.traceLocationCheckInRiskStates ?: emptyList()
        }

    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        latestRiskLevelResult.map {
            it?.presenceTracingDayRisk ?: emptyList()
        }

    /**
     * We delete warning packages after processing, we need to store the latest matches independent of success state
     */
    internal suspend fun reportCalculation(
        successful: Boolean,
        newOverlaps: List<CheckInWarningOverlap> = emptyList()
    ) {
        Timber.v("reportCalculation(successful=%b, newOverlaps=%s)", successful, newOverlaps)

        // delete stale matches from new packages, old matches are superseeded
        newOverlaps.map { it.traceWarningPackageId }.distinct().forEach {
            traceTimeIntervalMatchDao.deleteMatchesForPackage(it)
        }

        if (newOverlaps.isNotEmpty()) {
            traceTimeIntervalMatchDao.insert(newOverlaps.map { it.toTraceTimeIntervalMatchEntity() })
        }

        val result = calculateRiskResult(successful)
        addResultToDb(result)
    }

    private suspend fun calculateRiskResult(successful: Boolean): PtRiskLevelResult {
        val nowUtc = timeStamper.nowUTC
        val deadline = checkInsFilter.calculateDeadline(nowUtc)

        val riskState = if (successful) {
            val filteredOverlaps = checkInsFilter.filterCheckInWarningsByAge(
                allCheckInWarningOverlaps.first(),
                deadline
            )
            ptRiskCalculator.calculateTotalRisk(filteredOverlaps.calculateNormalizedTime())
        } else {
            RiskState.CALCULATION_FAILED
        }

        return PtRiskLevelResult(
            calculatedAt = nowUtc,
            calculatedFrom = deadline,
            riskState = riskState
        )
    }

    internal suspend fun deleteStaleData() {
        Timber.d("deleteStaleData()")
        traceTimeIntervalMatchDao.deleteOlderThan(retentionTime.toEpochMilli())
        riskLevelResultDao.deleteOlderThan(retentionTime.toEpochMilli())
    }

    private val retentionTime: Instant
        get() = timeStamper.nowUTC.minus(Duration.ofDays(15))

    suspend fun deleteAllMatches() {
        Timber.d("deleteAllMatches()")
        traceTimeIntervalMatchDao.deleteAll()
    }

    private fun addResultToDb(result: PtRiskLevelResult) {
        Timber.i("Saving risk calculation from ${result.calculatedAt} with result ${result.riskState}.")
        riskLevelResultDao.insert(result.toRiskLevelEntity())
    }

    private suspend fun List<CheckInWarningOverlap>.calculateNormalizedTime(): List<CheckInNormalizedTime> {
        return ptRiskCalculator.calculateNormalizedTime(this)
    }

    suspend fun clearAllTables() {
        Timber.i("Deleting all matches and results.")
        traceTimeIntervalMatchDao.deleteAll()
        riskLevelResultDao.deleteAll()
    }

    suspend fun clearResults() {
        Timber.i("Deleting all results.")
        riskLevelResultDao.deleteAll()
    }
}
