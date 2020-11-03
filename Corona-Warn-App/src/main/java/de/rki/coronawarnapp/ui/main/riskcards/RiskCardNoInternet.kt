package de.rki.coronawarnapp.ui.main.riskcards

import android.content.Context
import android.text.format.DateUtils
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import java.util.Date

data class RiskCardNoInternet(
    val lastRiskLevelScoreCalculated: Int,
    val lastRiskActualisation: Date?
) {

    fun getLastCalculatedRiskScore(c: Context): String {
        return when (lastRiskLevelScoreCalculated) {
            RiskLevelConstants.INCREASED_RISK ->
                c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(R.string.risk_card_increased_risk_headline)
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ->
                c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(R.string.risk_card_outdated_risk_headline)
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
                c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(R.string.risk_card_no_calculation_possible_headline)
            RiskLevelConstants.LOW_LEVEL_RISK ->
                c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(R.string.risk_card_low_risk_headline)
            RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
                c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(R.string.risk_card_unknown_risk_headline)
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ->
                c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(R.string.risk_card_unknown_risk_headline)
            else -> c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
        }
    }

    fun getLastTimeFetched(c: Context): String {
        return if (lastRiskActualisation != null) {
            c.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(c, lastRiskActualisation)
            )
        } else {
            c.getString(R.string.risk_card_body_not_yet_fetched)
        }
    }

    private fun formatRelativeDateTimeString(c: Context, date: Date): CharSequence? =
        DateUtils.getRelativeDateTimeString(
            c,
            date.time,
            DateUtils.DAY_IN_MILLIS,
            DateUtils.DAY_IN_MILLIS * 2,
            0
        )
}
