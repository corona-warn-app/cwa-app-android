package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.task.Task
import java.time.Instant

data class EwRiskLevelTaskResult(
    override val calculatedAt: Instant,
    override val failureReason: EwRiskLevelResult.FailureReason?,
    override val ewAggregatedRiskResult: EwAggregatedRiskResult?,
    override val exposureWindows: List<ExposureWindow>?
) : Task.Result, EwRiskLevelResult {

    constructor(
        calculatedAt: Instant,
        ewAggregatedRiskResult: EwAggregatedRiskResult,
        exposureWindows: List<ExposureWindow>?
    ) : this(
        calculatedAt = calculatedAt,
        ewAggregatedRiskResult = ewAggregatedRiskResult,
        exposureWindows = exposureWindows,
        failureReason = null
    )

    constructor(
        calculatedAt: Instant,
        failureReason: EwRiskLevelResult.FailureReason
    ) : this(
        calculatedAt = calculatedAt,
        failureReason = failureReason,
        ewAggregatedRiskResult = null,
        exposureWindows = null
    )

    override fun toString(): String = "RiskLevelTaskResult(" +
        "calculatedAt=$calculatedAt, " +
        "failureReason=$failureReason, " +
        "ewAggregatedRiskResult=$ewAggregatedRiskResult, " +
        "exposureWindows.size=${exposureWindows?.size}" +
        ")"
}
