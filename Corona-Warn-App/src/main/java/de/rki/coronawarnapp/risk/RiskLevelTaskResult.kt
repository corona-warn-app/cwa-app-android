package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.task.Task
import org.joda.time.Instant

data class RiskLevelTaskResult(
    override val riskLevel: RiskLevel,
    override val calculatedAt: Instant,
    override val aggregatedRiskResult: AggregatedRiskResult? = null,
    override val exposureWindows: List<ExposureWindow>? = null
) : Task.Result, RiskLevelResult
