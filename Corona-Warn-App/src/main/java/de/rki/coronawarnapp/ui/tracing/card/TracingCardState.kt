package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.tracing.TracingStatus
import de.rki.coronawarnapp.ui.tracing.common.BaseTracingState
import java.util.Date

data class TracingCardState(
    override val tracingStatus: TracingStatus.Status,
    override val riskLevelScore: Int,
    override val isRefreshing: Boolean,
    override val riskLevelLastSuccessfulCalculation: Int,
    override val matchedKeyCount: Int,
    override val daysSinceLastExposure: Int,
    override val activeTracingDaysInRetentionPeriod: Long,
    override val lastTimeDiagnosisKeysFetched: Date?,
    override val isBackgroundJobEnabled: Boolean,
    override val isManualKeyRetrievalEnabled: Boolean,
    override val manualKeyRetrievalTime: Long
) : BaseTracingState() {

    override val showDetails: Boolean = false

    /**
     * Formats the risk card icon color depending on risk level
     * This special handling is required due to light / dark mode differences and switches
     * between colored / light / dark background
     */
    fun getStableIconColor(c: Context): Int = c.getColor(
        if (!isTracingOffRiskLevel()) R.color.colorStableLight else R.color.colorTextSemanticNeutral
    )

    /**
     * Formats the risk card text display depending on risk level
     * for general information when no definite risk level
     * can be calculated
     */
    fun getRiskBody(c: Context): String = when (riskLevelScore) {
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> R.string.risk_card_outdated_risk_body
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> R.string.risk_card_body_tracing_off
        RiskLevelConstants.UNKNOWN_RISK_INITIAL -> R.string.risk_card_unknown_risk_body
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> R.string.risk_card_outdated_manual_risk_body
        else -> null
    }?.let { c.getString(it) } ?: ""

    /**
     * Formats the risk card text display of last persisted risk level
     * only in the special case where tracing is turned off and
     * the persisted risk level is of importance
     */
    fun getSavedRiskBody(c: Context): String {
        return if (
            riskLevelScore == RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ||
            riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ||
            riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
        ) {
            when (riskLevelLastSuccessfulCalculation) {
                RiskLevelConstants.LOW_LEVEL_RISK,
                RiskLevelConstants.INCREASED_RISK,
                RiskLevelConstants.UNKNOWN_RISK_INITIAL -> {
                    val arg = formatRiskLevelHeadline(c, riskLevelLastSuccessfulCalculation, false)
                    c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                        .format(arg)
                }
                else -> ""
            }
        } else {
            ""
        }
    }

    /**
     * Formats the risk card text display of infected contacts recognized
     */
    fun getRiskContactBody(c: Context): String {
        val resources = c.resources
        val contacts = matchedKeyCount
        return when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK -> {
                if (matchedKeyCount == 0) {
                    c.getString(R.string.risk_card_body_contact)
                } else {
                    resources.getQuantityString(
                        R.plurals.risk_card_body_contact_value_high_risk,
                        contacts,
                        contacts
                    )
                }
            }
            RiskLevelConstants.LOW_LEVEL_RISK -> {
                if (matchedKeyCount == 0) {
                    c.getString(R.string.risk_card_body_contact)
                } else {
                    resources.getQuantityString(
                        R.plurals.risk_card_body_contact_value,
                        contacts,
                        contacts
                    )
                }
            }
            else -> ""
        }
    }

    /**
     * Formats the risk card icon display of infected contacts recognized
     */
    fun getRiskContactIcon(c: Context): Drawable? = c.getDrawable(
        if (riskLevelScore == RiskLevelConstants.INCREASED_RISK) {
            R.drawable.ic_risk_card_contact_increased
        } else {
            R.drawable.ic_risk_card_contact
        }
    )

    /**
     * Formats the risk card text display of time since the last infected contact was recognized
     * only in the special case of increased risk as a positive contact is a
     * prerequisite for increased risk
     */
    fun getRiskContactLast(c: Context): String {
        val resources = c.resources
        val days = daysSinceLastExposure
        return if (riskLevelScore == RiskLevelConstants.INCREASED_RISK) {
            resources.getQuantityString(
                R.plurals.risk_card_increased_risk_body_contact_last,
                days,
                days
            )
        } else {
            ""
        }
    }

    /**
     * Formats the risk card text display of tracing active duration in days depending on risk level
     * Special case for increased risk as it is then only displayed on risk detail view
     */
    fun getRiskActiveTracingDaysInRetentionPeriod(c: Context): String = when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> {
            if (showDetails) {
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
            } else {
                ""
            }
        }
        RiskLevelConstants.LOW_LEVEL_RISK ->
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

        else -> ""
    }

    private fun formatRelativeDateTimeString(c: Context, date: Date): CharSequence? =
        DateUtils.getRelativeDateTimeString(
            c,
            date.time,
            DateUtils.DAY_IN_MILLIS,
            DateUtils.DAY_IN_MILLIS * 2,
            0
        )

    /**
     * /**
     * Formats the risk card text display of the last time diagnosis keys were
     * successfully fetched from the server
    */
     */
    fun getTimeFetched(c: Context): String {
        return when (riskLevelScore) {
            RiskLevelConstants.LOW_LEVEL_RISK,
            RiskLevelConstants.INCREASED_RISK -> {
                if (lastTimeDiagnosisKeysFetched != null) {
                    c.getString(
                        R.string.risk_card_body_time_fetched,
                        formatRelativeDateTimeString(c, lastTimeDiagnosisKeysFetched)
                    )
                } else {
                    c.getString(R.string.risk_card_body_not_yet_fetched)
                }
            }
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> {
                when (riskLevelLastSuccessfulCalculation) {
                    RiskLevelConstants.LOW_LEVEL_RISK,
                    RiskLevelConstants.INCREASED_RISK,
                    RiskLevelConstants.UNKNOWN_RISK_INITIAL -> {
                        if (lastTimeDiagnosisKeysFetched != null) {
                            c.getString(
                                R.string.risk_card_body_time_fetched,
                                formatRelativeDateTimeString(c, lastTimeDiagnosisKeysFetched)
                            )
                        } else {
                            c.getString(R.string.risk_card_body_not_yet_fetched)
                        }
                    }
                    else -> ""
                }
            }
            else -> ""
        }
    }

    /**
     * Formats the risk card text display of time when diagnosis keys will be updated
     * from server again when applicable
     */
    fun getNextUpdate(c: Context): String = if (!isBackgroundJobEnabled) {
        ""
    } else {
        when (riskLevelScore) {
            RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            RiskLevelConstants.LOW_LEVEL_RISK,
            RiskLevelConstants.INCREASED_RISK -> c.getString(
                R.string.risk_card_body_next_update
            )
            else -> ""
        }
    }

    /**
     * Formats the risk card divider color depending on risk level
     * This special handling is required due to light / dark mode differences and switches
     * between colored / light / dark background
     */
    fun getStableDividerColor(c: Context): Int = c.getColor(
        if (!isTracingOffRiskLevel()) R.color.colorStableHairlineLight else R.color.colorStableHairlineDark
    )

    /**
     * Formats the risk card button display for enable tracing depending on risk level and current view
     */
    fun showTracingButton(c: Context): Boolean = isTracingOffRiskLevel() && !showDetails

    /**
     * Formats the risk card button display for manual updates depending on risk level,
     * background task setting and current view
     */
    fun showUpdateButton(c: Context): Boolean =
        !isTracingOffRiskLevel() && !isBackgroundJobEnabled && !showDetails

    /**
     * Formats the risk card headline depending on risk level
     * Special case of a running update is caught
     */
    fun getRiskLevelHeadline(c: Context): String =
        formatRiskLevelHeadline(c, riskLevelScore, isRefreshing)

    private fun formatRiskLevelHeadline(c: Context, riskLevelScore: Int, isRefreshing: Boolean) =
        if (isRefreshing) {
            c.getString(R.string.risk_card_loading_headline)
        } else {
            when (riskLevelScore) {
                RiskLevelConstants.INCREASED_RISK -> c.getString(R.string.risk_card_increased_risk_headline)
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ->
                    c.getString(R.string.risk_card_outdated_risk_headline)
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
                    c.getString(R.string.risk_card_no_calculation_possible_headline)
                RiskLevelConstants.LOW_LEVEL_RISK -> c.getString(R.string.risk_card_low_risk_headline)
                RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
                    c.getString(R.string.risk_card_unknown_risk_headline)
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ->
                    c.getString(R.string.risk_card_unknown_risk_headline)
                else -> ""
            }
        }
}
