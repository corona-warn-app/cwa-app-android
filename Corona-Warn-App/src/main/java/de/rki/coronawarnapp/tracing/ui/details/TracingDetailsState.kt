package de.rki.coronawarnapp.tracing.ui.details

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

data class TracingDetailsState(
    val tracingStatus: GeneralTracingStatus.Status,
    val riskState: RiskState,
    val isManualKeyRetrievalEnabled: Boolean
) {

    fun getRiskColor(c: Context): Int = when {
        tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorSemanticUnknownRisk
        riskState == RiskState.INCREASED_RISK -> R.color.colorSemanticHighRisk
        riskState == RiskState.LOW_RISK -> R.color.colorSemanticLowRisk
        else -> R.color.colorSemanticUnknownRisk
    }.let { c.getColorCompat(it) }

    fun getStableTextColor(c: Context): Int = when {
        tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorTextPrimary1
        riskState == RiskState.INCREASED_RISK ||
            riskState == RiskState.LOW_RISK -> R.color.colorTextPrimary1InvertedStable
        else -> R.color.colorTextPrimary1
    }.let { c.getColorCompat(it) }

    fun isUpdateButtonEnabled(): Boolean = isManualKeyRetrievalEnabled

    fun isRiskDetailsEnableTracingButtonVisible(): Boolean =
        tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE

    fun isRiskDetailsUpdateButtonVisible(): Boolean =
        tracingStatus != GeneralTracingStatus.Status.TRACING_INACTIVE && isManualKeyRetrievalEnabled

    fun isRiskLevelButtonGroupVisible(): Boolean = isRiskDetailsEnableTracingButtonVisible() ||
        isRiskDetailsUpdateButtonVisible()

    fun getUpdateButtonText(c: Context): String = if (riskState == RiskState.CALCULATION_FAILED) {
        c.getString(R.string.risk_card_check_failed_no_internet_restart_button)
    } else {
        c.getString(R.string.risk_card_button_update)
    }
}
