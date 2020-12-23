package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.task.Task
import org.joda.time.Instant

data class RiskLevelTaskResult(
    override val calculatedAt: Instant,
    override val failureReason: RiskLevelResult.FailureReason?,
    override val aggregatedRiskResult: AggregatedRiskResult?,
    override val exposureWindows: List<ExposureWindow>?
) : Task.Result, RiskLevelResult {

    constructor(
        calculatedAt: Instant,
        aggregatedRiskResult: AggregatedRiskResult,
        exposureWindows: List<ExposureWindow>?
    ) : this(
        calculatedAt = calculatedAt,
        aggregatedRiskResult = aggregatedRiskResult,
        exposureWindows = exposureWindows,
        failureReason = null
    )

    constructor(
        calculatedAt: Instant,
        failureReason: RiskLevelResult.FailureReason
    ) : this(
        calculatedAt = calculatedAt,
        failureReason = failureReason,
        aggregatedRiskResult = null,
        exposureWindows = null
    )

    override fun toString(): String = "RiskLevelTaskResult(" +
        "calculatedAt=$calculatedAt, " +
        "failureReason=$failureReason, " +
        "aggregatedRiskResult=$aggregatedRiskResult, " +
        "exposureWindows.size=${exposureWindows?.size}" +
        ")"
}
