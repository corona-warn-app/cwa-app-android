package de.rki.coronawarnapp.risk.storage.internal

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtDayRisk
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.mapToRiskState
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.util.TimeStamper
import java.time.Instant
import javax.inject.Inject

@Reusable
class RiskCombinator @Inject constructor(
    private val timeStamper: TimeStamper
) {

    private val initialEWRiskLevelResult = object : EwRiskLevelResult {
        override val calculatedAt: Instant = Instant.EPOCH
        override val riskState: RiskState = RiskState.LOW_RISK
        override val failureReason: EwRiskLevelResult.FailureReason? = null
        override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
    }

    private val initialPTRiskLevelResult: PtRiskLevelResult = PtRiskLevelResult(
        calculatedAt = Instant.EPOCH,
        riskState = RiskState.LOW_RISK,
        calculatedFrom = Instant.EPOCH
    )

    internal val initialCombinedResult = CombinedEwPtRiskLevelResult(
        ptRiskLevelResult = initialPTRiskLevelResult,
        ewRiskLevelResult = initialEWRiskLevelResult
    )

    private val ewCurrentLowRiskLevelResult
        get() = object : EwRiskLevelResult {
            override val calculatedAt: Instant = timeStamper.nowUTC
            override val riskState: RiskState = RiskState.LOW_RISK
            override val failureReason: EwRiskLevelResult.FailureReason? = null
            override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
            override val exposureWindows: List<ExposureWindow>? = null
            override val matchedKeyCount: Int = 0
        }

    private val ptCurrentLowRiskLevelResult: PtRiskLevelResult
        get() {
            val now = timeStamper.nowUTC
            return PtRiskLevelResult(
                calculatedAt = now,
                riskState = RiskState.LOW_RISK,
                calculatedFrom = now
            )
        }

    internal val latestCombinedResult: CombinedEwPtRiskLevelResult
        get() = CombinedEwPtRiskLevelResult(
            ptCurrentLowRiskLevelResult,
            ewCurrentLowRiskLevelResult
        )

    internal fun combineEwPtRiskLevelResults(
        ptRiskResults: List<PtRiskLevelResult>,
        ewRiskResults: List<EwRiskLevelResult>
    ): List<CombinedEwPtRiskLevelResult> {
        val allDates =
            ptRiskResults.map { it.calculatedAt }.plus(ewRiskResults.map { it.calculatedAt }).distinct()
        val sortedPtResults = ptRiskResults.sortedByDescending { it.calculatedAt }
        val sortedEwResults = ewRiskResults.sortedByDescending { it.calculatedAt }
        return allDates.map { date ->
            val ptRisk = sortedPtResults.find {
                // Consider only presence tracing "successful" risk calculation. See EXPOSUREAPP-13383
                it.calculatedAt <= date && it.riskState != RiskState.CALCULATION_FAILED
            } ?: initialPTRiskLevelResult
            val ewRisk = sortedEwResults.find {
                it.calculatedAt <= date
            } ?: initialEWRiskLevelResult

            CombinedEwPtRiskLevelResult(
                ptRiskLevelResult = ptRisk,
                ewRiskLevelResult = ewRisk
            )
        }
    }

    internal fun combineRisk(
        ptRiskList: List<PresenceTracingDayRisk>,
        exposureWindowDayRiskList: List<ExposureWindowDayRisk>
    ): List<CombinedEwPtDayRisk> {
        val allDates =
            ptRiskList.map {
                it.localDateUtc
            }.plus(
                exposureWindowDayRiskList.map { it.localDateUtc }
            ).distinct()
        return allDates.map { date ->
            val ptRisk = ptRiskList.find { it.localDateUtc == date }
            val ewRisk = exposureWindowDayRiskList.find { it.localDateUtc == date }
            CombinedEwPtDayRisk(
                localDate = date,
                riskState = combine(
                    ptRisk?.riskState,
                    ewRisk?.riskLevel?.mapToRiskState()
                )
            )
        }
    }

    companion object {
        fun combine(vararg states: RiskState?): RiskState {
            if (states.any { it == RiskState.CALCULATION_FAILED }) return RiskState.CALCULATION_FAILED
            if (states.any { it == RiskState.INCREASED_RISK }) return RiskState.INCREASED_RISK

            require(states.filterNotNull().all { it == RiskState.LOW_RISK })

            return RiskState.LOW_RISK
        }
    }
}
