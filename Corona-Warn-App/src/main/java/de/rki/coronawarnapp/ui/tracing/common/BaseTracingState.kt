package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.TracingStatus
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHMS
import java.util.Date

abstract class BaseTracingState {
    abstract val tracingStatus: TracingStatus.Status
    abstract val riskLevelScore: Int
    abstract val isRefreshing: Boolean
    abstract val riskLevelLastSuccessfulCalculation: Int
    abstract val matchedKeyCount: Int
    abstract val daysSinceLastExposure: Int
    abstract val activeTracingDaysInRetentionPeriod: Int
    abstract val lastTimeDiagnosisKeysFetched: Date?
    abstract val isBackgroundJobEnabled: Boolean
    abstract val showDetails: Boolean // Only true for riskdetailsfragment
    abstract val isManualKeyRetrievalEnabled: Boolean
    abstract val manualKeyRetrievalTime: Long

    fun getRiskShape(c: Context): Drawable? =
        c.getDrawable(if (showDetails) R.drawable.rectangle else R.drawable.card)

    /**
     * Formats the risk card colors for default and pressed states depending on risk level
     */
    fun getRiskColor(c: Context): Int {
        val colorRes = when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK -> R.color.colorSemanticHighRisk
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> R.color.colorSemanticUnknownRisk
            RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorSemanticLowRisk
            else -> R.color.colorSemanticNeutralRisk
        }
        return c.getColor(colorRes)
    }

    /**
     * Formats the risk card colors for default and pressed states depending on risk level
     */
    fun getRiskColorStateList(c: Context): ColorStateList? {
        val resource = when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK -> R.color.card_increased
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> R.color.card_outdated
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> R.color.card_no_calculation
            RiskLevelConstants.LOW_LEVEL_RISK -> R.color.card_low
            else -> R.color.card_unknown
        }
        return c.getColorStateList(resource)
    }

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

    fun isUpdateButtonEnabled(c: Context): Boolean {
        return isManualKeyRetrievalEnabled
    }
}
