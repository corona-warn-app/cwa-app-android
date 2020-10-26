package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHMS
import java.util.Date

abstract class BaseTracingState {
    abstract val tracingStatus: GeneralTracingStatus.Status
    abstract val riskLevelScore: Int
    abstract val tracingProgress: TracingProgress
    abstract val lastRiskLevelScoreCalculated: Int
    abstract val matchedKeyCount: Int
    abstract val daysSinceLastExposure: Int
    abstract val activeTracingDaysInRetentionPeriod: Long
    abstract val lastTimeDiagnosisKeysFetched: Date?
    abstract val isBackgroundJobEnabled: Boolean
    abstract val showDetails: Boolean // Only true for riskdetailsfragment
    abstract val isManualKeyRetrievalEnabled: Boolean
    abstract val manualKeyRetrievalTime: Long

    /**
     * Formats the risk card colors for default and pressed states depending on risk level
     */
    fun getRiskColor(c: Context): Int = when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> R.color.colorSemanticHighRisk
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> R.color.colorSemanticUnknownRisk
        RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorSemanticLowRisk
        else -> R.color.colorSemanticNeutralRisk
    }.let { c.getColor(it) }

    fun isTracingOffRiskLevel(): Boolean = when (riskLevelScore) {
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> true
        else -> false
    }

    fun getStableTextColor(c: Context): Int = c.getColor(
        if (!isTracingOffRiskLevel()) R.color.colorStableLight else R.color.colorTextPrimary1
    )

    /**
     * Change the manual update button text according to current timer
     */
    fun getUpdateButtonText(c: Context): String = if (manualKeyRetrievalTime <= 0) {
        c.getString(R.string.risk_card_button_update)
    } else {
        val hmsCooldownTime = manualKeyRetrievalTime.millisecondsToHMS()
        c.getString(R.string.risk_card_button_cooldown).format(hmsCooldownTime)
    }

    fun isUpdateButtonEnabled(): Boolean = isManualKeyRetrievalEnabled
}
