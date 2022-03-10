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
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.joda.time.Days
import org.joda.time.Instant
import timber.log.Timber
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

    val allOverlaps = traceTimeIntervalMatchDao.allMatches().map { entities ->
        entities.map { it.toCheckInWarningOverlap() }
    }

    val latestRiskLevelResult: Flow<PtRiskLevelResult?> =
        combine(
            riskLevelResultDao.allEntries(),
            allOverlaps,
        ) { resultList, overlaps ->
            val latestResult = resultList.maxByOrNull {
                it.calculatedAtMillis
            }
            val relevantWarnings = checkInsFilter.filterCheckInWarningsByAge(
                allOverlaps.first(),
                Instant.ofEpochMilli(latestResult?.calculatedFromMillis ?: 0)
            )
            val normalizedTime = relevantWarnings.calculateNormalizedTime()

            latestResult?.toRiskLevelResult(
                presenceTracingDayRisks = ptRiskCalculator.calculateDayRisk(normalizedTime),
                traceLocationCheckInRiskStates = ptRiskCalculator.calculateCheckInRiskPerDay(normalizedTime),
                checkInWarningOverlaps = relevantWarnings,
            )
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
        overlaps: List<CheckInWarningOverlap> = emptyList()
    ) {
        Timber.v("reportCalculation(successful=%b, overlaps=%s)", successful, overlaps)

        val nowUtc = timeStamper.nowUTC

        // delete stale matches from new packages, old matches are superseeded
        overlaps.map { it.traceWarningPackageId }.forEach {
            traceTimeIntervalMatchDao.deleteMatchesForPackage(it)
        }

        if (overlaps.isNotEmpty()) {
            traceTimeIntervalMatchDao.insert(overlaps.map { it.toTraceTimeIntervalMatchEntity() })
        }

        val deadline = checkInsFilter.getDeadline(nowUtc)

        val result = if (successful) {
            val filteredOverlaps = checkInsFilter.filterCheckInWarningsByAge(
                allOverlaps.first(),
                deadline
            )
            val risk = ptRiskCalculator.calculateTotalRisk(filteredOverlaps.calculateNormalizedTime())
            PtRiskLevelResult(
                calculatedAt = nowUtc,
                calculatedFrom = deadline,
                riskState = risk)
        } else {
            PtRiskLevelResult(
                calculatedAt = nowUtc,
                calculatedFrom = deadline,
                riskState = RiskState.CALCULATION_FAILED)
        }
        addResult(result)
    }

    internal suspend fun deleteStaleData() {
        Timber.d("deleteStaleData()")
        traceTimeIntervalMatchDao.deleteOlderThan(retentionTime.millis)
        riskLevelResultDao.deleteOlderThan(retentionTime.millis)
    }

    private val retentionTime: Instant
        get() = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())

    suspend fun deleteAllMatches() {
        Timber.d("deleteAllMatches()")
        traceTimeIntervalMatchDao.deleteAll()
    }

    fun allEntries() = riskLevelResultDao.allEntries().map { list ->
        list.sortedByDescending {
            it.calculatedAtMillis
        }.map {
            it.toRiskLevelResult(
                presenceTracingDayRisks = null,
                checkInWarningOverlaps = null,
            )
        }
    }

    private fun addResult(result: PtRiskLevelResult) {
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
