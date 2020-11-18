package de.rki.coronawarnapp.ui.main.riskcards

import android.content.Context
import android.text.format.DateUtils
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import java.util.Date

sealed class RiskCardsHandler {

    abstract val lastRiskActualisation: Date?

    // Used in all risk cards to display when the last risk calculation was performed.
    fun getLastTimeFetched(c: Context): String {
        val lastValue = lastRiskActualisation ?: return c.getString(R.string.risk_card_body_not_yet_fetched)
        return c.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(c, lastValue)
        )
    }

    // Helper function for formatting last risk caluclation text
    // TODO: create an extension function over Context that we could then call in getLastTimeFetched()
    private fun formatRelativeDateTimeString(c: Context, date: Date): CharSequence =
        DateUtils.getRelativeDateTimeString(
            c,
            date.time,
            DateUtils.DAY_IN_MILLIS,
            DateUtils.DAY_IN_MILLIS * 2,
            0
        )

    // Sealed class to set the common attributes found in both 'Low Risk' and 'High Risk' cards
    sealed class LowHighRiskHandler : RiskCardsHandler() {

        abstract val activeTracingDaysInRetentionPeriod: Long
        abstract val tracingStatus: GeneralTracingStatus.Status
        abstract val riskLevelScore: Int

        fun getTracingDaysInRetentionPeriod(c: Context): String =
            if (activeTracingDaysInRetentionPeriod < TimeVariables.getDefaultRetentionPeriodInDays()) {
                c.getString(
                    R.string.risk_card_body_saved_days
                )
                    .format(activeTracingDaysInRetentionPeriod)
            } else {
                c.getString(
                    R.string.risk_card_body_saved_days_full
                )
            }

        // Replacement for showUpdateButton and isTracingOff form TracingCardState.kt
        fun displaySyncButtonWhenManualModeIsTurnedOn(): Boolean {
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

        data class RiskCardLowRisk(
            override val lastRiskActualisation: Date?,
            override val activeTracingDaysInRetentionPeriod: Long,
            override val tracingStatus: GeneralTracingStatus.Status,
            override val riskLevelScore: Int
        ) : LowHighRiskHandler()
    }

    // Sealed class to set the attributes from 'Stopped Risk' and 'Failed Risk' cards
    sealed class StoppedOrFailedRisk : RiskCardsHandler() {

        abstract val lastRiskLevelScoreCalculated: Int

        fun getLastCalculatedRiskScore(c: Context): String {
            return when (lastRiskLevelScoreCalculated) {
                RiskLevelConstants.INCREASED_RISK ->
                    c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                        .format(c.getString(R.string.risk_card_increased_risk_headline))
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ->
                    c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                        .format(c.getString(R.string.risk_card_outdated_risk_headline))
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
                    c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                        .format(c.getString(R.string.risk_card_no_calculation_possible_headline))
                RiskLevelConstants.LOW_LEVEL_RISK ->
                    c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                        .format(c.getString(R.string.risk_card_low_risk_headline))
                RiskLevelConstants.UNKNOWN_RISK_INITIAL,
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ->
                    c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                        .format(c.getString(R.string.risk_card_unknown_risk_headline))
                else -> c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
            }
        }

        data class RiskCardStoppedOrFailedRisk(
            override val lastRiskActualisation: Date?,
            override val lastRiskLevelScoreCalculated: Int
        ) : StoppedOrFailedRisk()
    }
}
