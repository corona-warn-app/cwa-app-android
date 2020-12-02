package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress

abstract class BaseTracingState {
    abstract val tracingStatus: GeneralTracingStatus.Status
    abstract val riskState: RiskState
    abstract val tracingProgress: TracingProgress
    abstract val showDetails: Boolean // Only true for riskdetailsfragment
    abstract val isManualKeyRetrievalEnabled: Boolean

    /**
     * Formats the risk card colors for default and pressed states depending on risk level
     */
    fun getRiskColor(c: Context): Int = when {
        tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorSemanticUnknownRisk
        riskState == RiskState.INCREASED_RISK -> R.color.colorSemanticHighRisk
        riskState == RiskState.LOW_RISK -> R.color.colorSemanticLowRisk
        else -> R.color.colorSemanticUnknownRisk
    }.let { c.getColor(it) }

    fun isTracingOff(): Boolean = tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE

    fun getStableTextColor(c: Context): Int = when {
        tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorTextPrimary1
        riskState == RiskState.INCREASED_RISK ||
            riskState == RiskState.LOW_RISK -> R.color.colorTextPrimary1InvertedStable
        else -> R.color.colorTextPrimary1
    }.let { c.getColor(it) }

    /**
     * Change the manual update button text according to current timer
     */
    fun getUpdateButtonText(c: Context): String = if (riskState == RiskState.CALCULATION_FAILED) {
        c.getString(R.string.risk_card_check_failed_no_internet_restart_button)
    } else {
        c.getString(R.string.risk_card_button_update)
    }

    fun isUpdateButtonEnabled(): Boolean = isManualKeyRetrievalEnabled
}
