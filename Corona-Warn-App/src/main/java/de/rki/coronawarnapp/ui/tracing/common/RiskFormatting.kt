@file:JvmName("RiskFormatting")

package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants

/**
 * Formats the risk details suggested behavior icon color depending on risk level
 * This special handling is required due to light / dark mode differences and switches
 * between colored / light / dark background
 *
 * @param riskLevelScore
 * @return
 */
fun formatBehaviorIcon(context: Context, riskLevelScore: Int): Int {
    val colorRes = when (riskLevelScore) {
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> R.color.colorTextSemanticNeutral
        else -> R.color.colorStableLight
    }
    return context.getColor(colorRes)
}

/**
 * Formats the risk details suggested behavior icon background color depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
fun formatBehaviorIconBackground(context: Context, riskLevelScore: Int): Int {
    val colorRes = when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> R.color.colorSemanticHighRisk
        RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorSemanticLowRisk
        RiskLevelConstants.UNKNOWN_RISK_INITIAL -> R.color.colorSemanticNeutralRisk
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> R.color.colorSemanticNeutralRisk
        else -> R.color.colorSurface2
    }
    return context.getColor(colorRes)
}
