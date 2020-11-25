package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress

abstract class BaseTracingState {
    abstract val tracingStatus: GeneralTracingStatus.Status
    abstract val riskLevelScore: Int
    abstract val tracingProgress: TracingProgress
    abstract val showDetails: Boolean // Only true for riskdetailsfragment
    abstract val isManualKeyRetrievalEnabled: Boolean

    /**
     * Formats the risk card colors for default and pressed states depending on risk level
     */
    fun getRiskColor(c: Context): Int = when {
        tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorSemanticUnknownRisk
        riskLevelScore == RiskLevelConstants.INCREASED_RISK -> R.color.colorSemanticHighRisk
        riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorSemanticLowRisk
        else -> R.color.colorSemanticUnknownRisk
    }.let { c.getColor(it) }

    fun isTracingOffRiskLevel(): Boolean {
        return if (tracingStatus != GeneralTracingStatus.Status.TRACING_INACTIVE) {
            when (riskLevelScore) {
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> true
                else -> false
            }
        } else {
            return true
        }
    }

    fun getStableTextColor(c: Context): Int = when {
            tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorTextPrimary1
            riskLevelScore == RiskLevelConstants.INCREASED_RISK ||
            riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorTextPrimary1InvertedStable
            else -> R.color.colorTextPrimary1
        }.let { c.getColor(it) }

    /**
     * Change the manual update button text according to current timer
     */
    fun getUpdateButtonText(c: Context): String = if (riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET) {
        c.getString(R.string.risk_card_check_failed_no_internet_restart_button)
    } else {
        c.getString(R.string.risk_card_button_update)
    }

    fun isUpdateButtonEnabled(): Boolean = isManualKeyRetrievalEnabled ||
        riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET
}
