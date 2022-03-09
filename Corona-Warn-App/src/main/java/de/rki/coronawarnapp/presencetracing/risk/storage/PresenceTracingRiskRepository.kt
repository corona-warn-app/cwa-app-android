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
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import org.joda.time.Days
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskRepository @Inject constructor(
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val databaseFactory: PresenceTracingRiskDatabase.Factory,
    private val timeStamper: TimeStamper,
    private val checkInsFilter: CheckInsFilter,
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
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

    private val relevantNormalizedTime: HotDataFlow<List<CheckInNormalizedTime>> = HotDataFlow(
        loggingTag = "PresenceTracingRiskRepository",
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        val relevantWarnings = checkInsFilter.filterCheckInWarningsByAge(
            allOverlaps.first(),
            lastSuccessfulRiskResult.first()?.calculatedFrom ?: Instant.EPOCH
        )
        calculateNormalizedTime(relevantWarnings)
    }

    private suspend fun calculateNormalizedTime(list: List<CheckInWarningOverlap>): List<CheckInNormalizedTime> {
        return presenceTracingRiskCalculator.calculateNormalizedTime(list)
    }

    val lastSuccessfulRiskResult: Flow<PtRiskLevelResult?> = riskLevelResultDao.allEntries().map { list ->
        list.filter {
            it.riskState.isSuccessfulCalculation()
        }.sortAndComplementLatestResult().firstOrNull()
    }

    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        relevantNormalizedTime.data.map {
            presenceTracingRiskCalculator.calculateCheckInRiskPerDay(it)
        }

    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        relevantNormalizedTime.data.map {
            presenceTracingRiskCalculator.calculateDayRisk(it)
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
            val normalizedTime = calculateNormalizedTime(
                filteredOverlaps
            )
            val risk = presenceTracingRiskCalculator.calculateTotalRisk(normalizedTime)
            relevantNormalizedTime.updateBlocking {
                normalizedTime
            }
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
        traceTimeIntervalMatchDao.deleteOlderThan(fifteenDaysAgo.millis)
        riskLevelResultDao.deleteOlderThan(fifteenDaysAgo.millis)
    }

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

    private suspend fun List<PresenceTracingRiskLevelResultEntity>.sortAndComplementLatestResult() =
        sortedByDescending {
            it.calculatedAtMillis
        }
            .mapIndexed { index, entity ->
                if (index == 0) {
                    // add risk per day to the latest result
                    entity.toRiskLevelResult(
                        presenceTracingDayRisks = presenceTracingDayRisk.first(),
                        checkInWarningOverlaps = checkInsFilter.filterCheckInWarningsByAge(
                            allOverlaps.first(),
                            Instant.ofEpochMilli(entity.calculatedAtMillis)
                        ),
                    )
                } else {
                    entity.toRiskLevelResult(
                        presenceTracingDayRisks = null,
                        checkInWarningOverlaps = null,
                    )
                }
            }

    private fun addResult(result: PtRiskLevelResult) {
        Timber.i("Saving risk calculation from ${result.calculatedAt} with result ${result.riskState}.")
        riskLevelResultDao.insert(result.toRiskLevelEntity())
    }

    private val fifteenDaysAgo: Instant
        get() = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())

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
