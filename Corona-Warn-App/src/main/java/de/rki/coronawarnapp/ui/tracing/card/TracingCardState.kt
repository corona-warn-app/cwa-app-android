package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.ui.tracing.common.BaseTracingState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import java.util.Date

data class TracingCardState(
    override val tracingStatus: GeneralTracingStatus.Status,
    override val riskLevelScore: Int,
    override val tracingProgress: TracingProgress,
    val lastRiskLevelScoreCalculated: Int,
    val daysWithEncounters: Int,
    val lastEncounterAt: Instant?,
    val activeTracingDaysInRetentionPeriod: Long,
    val lastTimeDiagnosisKeysFetched: Date?,
    override val isManualKeyRetrievalEnabled: Boolean,
    override val showDetails: Boolean = false
) : BaseTracingState() {

    /**
     * Formats the risk card icon color depending on risk level
     * This special handling is required due to light / dark mode differences and switches
     * between colored / light / dark background
     */
    fun getStableIconColor(c: Context): Int = when {
        tracingStatus != GeneralTracingStatus.Status.TRACING_ACTIVE -> R.color.colorTextSemanticNeutral
        riskLevelScore == RiskLevelConstants.INCREASED_RISK ||
        riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorStableLight
        else -> R.color.colorTextSemanticNeutral
    }.let { c.getColor(it) }

    /**
     * Formats the risk card text display depending on risk level
     * for general information when no definite risk level
     * can be calculated
     */
    fun getRiskBody(c: Context): String {
        return if (tracingStatus != GeneralTracingStatus.Status.TRACING_INACTIVE) {
            when (riskLevelScore) {
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> R.string.risk_card_outdated_risk_body
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> R.string.risk_card_body_tracing_off
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> R.string.risk_card_outdated_manual_risk_body
                RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET -> R.string.risk_card_check_failed_no_internet_body
                else -> null
            }?.let { c.getString(it) } ?: ""
        } else {
            return c.getString(R.string.risk_card_body_tracing_off)
        }
    }

    /**
     * Formats the risk card text display of last persisted risk level
     * only in the special case where tracing is turned off and
     * the persisted risk level is of importance
     */
    fun getSavedRiskBody(c: Context): String {
        return if (tracingStatus != GeneralTracingStatus.Status.TRACING_INACTIVE) {
            return if (
                riskLevelScore == RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ||
                riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ||
                riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ||
                riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET
            ) {
                when (lastRiskLevelScoreCalculated) {
                    RiskLevelConstants.LOW_LEVEL_RISK,
                    RiskLevelConstants.INCREASED_RISK -> {
                        val arg = formatRiskLevelHeadline(c, lastRiskLevelScoreCalculated)
                        c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                            .format(arg)
                    }
                    else -> ""
                }
            } else {
                ""
            }
        } else {
            val arg = formatRiskLevelHeadline(c, lastRiskLevelScoreCalculated)
            c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(arg)
        }
    }

    /**
     * Formats the risk card text display of infected contacts recognized
     */
    fun getRiskContactBody(c: Context): String {
        val resources = c.resources
        return when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK -> {
                if (daysWithEncounters == 0) {
                    c.getString(R.string.risk_card_high_risk_no_encounters_body)
                } else {
                    resources.getQuantityString(
                        R.plurals.risk_card_high_risk_encounter_days_body,
                        daysWithEncounters,
                        daysWithEncounters
                    )
                }
            }
            RiskLevelConstants.LOW_LEVEL_RISK -> {
                if (daysWithEncounters == 0) {
                    c.getString(R.string.risk_card_low_risk_no_encounters_body)
                } else {
                    resources.getQuantityString(
                        R.plurals.risk_card_low_risk_encounter_days_body,
                        daysWithEncounters,
                        daysWithEncounters
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
    fun getRiskContactLast(c: Context): String = if (riskLevelScore == RiskLevelConstants.INCREASED_RISK) {
        val formattedDate = lastEncounterAt?.toLocalDate()?.toString(DateTimeFormat.mediumDate())
        c.getString(R.string.risk_card_high_risk_most_recent_body, formattedDate)
    } else {
        ""
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
        if (tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE) {
            return if (lastTimeDiagnosisKeysFetched != null) {
                c.getString(
                    R.string.risk_card_body_time_fetched,
                    formatRelativeDateTimeString(c, lastTimeDiagnosisKeysFetched)
                )
            } else {
                c.getString(R.string.risk_card_body_not_yet_fetched)
            }
        }
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
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL,
            RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET -> {
                when (lastRiskLevelScoreCalculated) {
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
                    else -> ""
                }
            }
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
    fun showTracingButton(): Boolean = isTracingOffRiskLevel() && !showDetails

    /**
     * Formats the risk card button display for manual updates depending on risk level,
     * background task setting and current view
     */
    fun showUpdateButton(): Boolean =
        !isTracingOffRiskLevel() &&
            (isManualKeyRetrievalEnabled || riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET) &&
            !showDetails

    fun getRiskLevelHeadline(c: Context) = formatRiskLevelHeadline(c, riskLevelScore)

    fun formatRiskLevelHeadline(c: Context, riskLevelScore: Int): String {
        return if (tracingStatus != GeneralTracingStatus.Status.TRACING_INACTIVE) {
            when (riskLevelScore) {
                RiskLevelConstants.INCREASED_RISK ->
                    R.string.risk_card_increased_risk_headline
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ->
                    R.string.risk_card_outdated_risk_headline
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
                    R.string.risk_card_no_calculation_possible_headline
                RiskLevelConstants.LOW_LEVEL_RISK ->
                    R.string.risk_card_low_risk_headline
                RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET -> R.string.risk_card_check_failed_no_internet_headline
                else -> null
            }?.let { c.getString(it) } ?: ""
        } else {
            return c.getString(R.string.risk_card_no_calculation_possible_headline)
        }
    }

    fun getProgressCardHeadline(c: Context): String = when (tracingProgress) {
        TracingProgress.Downloading -> R.string.risk_card_progress_download_headline
        TracingProgress.ENFIsCalculating -> R.string.risk_card_progress_calculation_headline
        TracingProgress.Idle -> null
    }?.let { c.getString(it) } ?: ""

    fun getProgressCardBody(c: Context): String = when (tracingProgress) {
        TracingProgress.Downloading -> R.string.risk_card_progress_download_body
        TracingProgress.ENFIsCalculating -> R.string.risk_card_progress_calculation_body
        TracingProgress.Idle -> null
    }?.let { c.getString(it) } ?: ""

    fun isTracingInProgress(): Boolean = tracingProgress != TracingProgress.Idle

    fun getRiskInfoContainerBackgroundTint(c: Context): ColorStateList {
        return if (tracingStatus != GeneralTracingStatus.Status.TRACING_INACTIVE) {
            when (riskLevelScore) {
                RiskLevelConstants.INCREASED_RISK -> R.color.card_increased
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> R.color.card_outdated
                RiskLevelConstants.LOW_LEVEL_RISK -> R.color.card_low
                else -> R.color.card_no_calculation
            }.let { c.getColorStateList(it) }
        } else {
            return c.getColorStateList(R.color.card_no_calculation)
        }
    }

    fun getUpdateButtonColor(c: Context): Int = when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK,
        RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorStableLight
        else -> R.color.colorAccentTintButton
    }.let { c.getColor(it) }

    fun getUpdateButtonTextColor(c: Context): Int = when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK,
        RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorTextPrimary1Stable
        else -> R.color.colorTextPrimary1InvertedStable
    }.let { c.getColor(it) }
}
