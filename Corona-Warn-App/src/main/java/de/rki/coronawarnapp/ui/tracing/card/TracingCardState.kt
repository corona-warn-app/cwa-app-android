package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.ui.tracing.common.BaseTracingState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.lists.HasStableId
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

@Suppress("TooManyFunctions")
data class TracingCardState(
    override val tracingStatus: GeneralTracingStatus.Status,
    override val riskState: RiskState,
    override val tracingProgress: TracingProgress,
    val lastSuccessfulRiskState: RiskState,
    val daysWithEncounters: Int,
    val lastEncounterAt: Instant?,
    val activeTracingDays: Long,
    val lastExposureDetectionTime: Instant?,
    override val isManualKeyRetrievalEnabled: Boolean,
    override val showDetails: Boolean = false
) : BaseTracingState(), HasStableId {

    override val stableId: Long
        get() = 1L

    /**
     * Formats the risk card text display depending on risk level
     * for general information when no definite risk level
     * can be calculated
     */
    fun getErrorStateBody(c: Context): String {
        if (isTracingOff()) {
            return c.getString(R.string.risk_card_body_tracing_off)
        }
        return when (riskState) {
            CALCULATION_FAILED -> c.getString(R.string.risk_card_check_failed_no_internet_body)
            else -> ""
        }
    }

    /**
     * Formats the risk card text display of infected contacts recognized
     */
    fun getRiskContactBody(c: Context): String = when {
        isTracingOff() -> {
            ""
        }
        riskState == INCREASED_RISK && daysWithEncounters == 0 -> {
            // LEGACY MIGRATION CASE FROM 1.7.x -> 1.8.x ('days with encounter' doesn't exit in 1.7.x)
            // see RiskLevelResultMigrator.kt
            ""
        }
        riskState == INCREASED_RISK -> {
            c.resources.getQuantityString(
                R.plurals.risk_card_high_risk_encounter_days_body,
                daysWithEncounters,
                daysWithEncounters
            )
        }
        riskState == LOW_RISK && daysWithEncounters == 0 -> {
            // caution! is 0 after migration from 1.7.x -> 1.8.x
            // see RiskLevelResultMigrator.kt
            c.getString(R.string.risk_card_low_risk_no_encounters_body)
        }
        riskState == LOW_RISK -> {
            c.resources.getQuantityString(
                R.plurals.risk_card_low_risk_encounter_days_body,
                daysWithEncounters,
                daysWithEncounters
            )
        }
        else -> ""
    }

    /**
     * Formats the risk card icon display of infected contacts recognized
     */
    fun getRiskContactIcon(c: Context): Drawable? = c.getDrawable(
        if (riskState == INCREASED_RISK) {
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
    fun getRiskContactLast(c: Context): String = when {
        isTracingOff() -> ""
        riskState == INCREASED_RISK && lastEncounterAt != null ->
            // caution! lastEncounterAt is null after migration from 1.7.x -> 1.8.x
            // see RiskLevelResultMigrator.kt
            c.getString(
                R.string.risk_card_high_risk_most_recent_body,
                lastEncounterAt.toLocalDate().toString(DateTimeFormat.mediumDate())
            )
        else -> ""
    }

    /**
     * Formats the risk card text display of tracing active duration in days depending on risk level
     * Special case for increased risk as it is then only displayed on risk detail view
     */
    fun getRiskActiveTracingDaysInRetentionPeriod(c: Context): String = when {
        isTracingOff() -> ""
        riskState == INCREASED_RISK && !showDetails -> ""
        riskState == INCREASED_RISK && activeTracingDays < TimeVariables.getDefaultRetentionPeriodInDays() -> {
            c.getString(R.string.risk_card_body_saved_days).format(activeTracingDays)
        }
        riskState == INCREASED_RISK && activeTracingDays >= TimeVariables.getDefaultRetentionPeriodInDays() -> {
            c.getString(R.string.risk_card_body_saved_days_full)
        }
        riskState == LOW_RISK && activeTracingDays < TimeVariables.getDefaultRetentionPeriodInDays() -> {
            c.getString(R.string.risk_card_body_saved_days).format(activeTracingDays)
        }
        riskState == LOW_RISK && activeTracingDays >= TimeVariables.getDefaultRetentionPeriodInDays() -> {
            c.getString(R.string.risk_card_body_saved_days_full)
        }
        else -> ""
    }

    private fun formatRelativeDateTimeString(c: Context, date: Instant): CharSequence? =
        DateUtils.getRelativeDateTimeString(
            c,
            date.millis,
            DateUtils.DAY_IN_MILLIS,
            DateUtils.DAY_IN_MILLIS * 2,
            0
        )

    /**
     * Formats the risk card divider color depending on risk level
     * This special handling is required due to light / dark mode differences and switches
     * between colored / light / dark background
     */
    fun getStableDividerColor(c: Context): Int = c.getColor(
        if (isTracingOff() || riskState == CALCULATION_FAILED) {
            R.color.colorStableHairlineDark
        } else {
            R.color.colorStableHairlineLight
        }
    )

    /**
     * Formats the risk card button display for enable tracing depending on risk level and current view
     */
    fun showTracingButton(): Boolean = isTracingOff() && !showDetails

    /**
     * Formats the risk card button display for manual updates depending on risk level,
     * background task setting and current view
     */
    fun showUpdateButton(): Boolean =
        !isTracingOff() &&
            (isManualKeyRetrievalEnabled || riskState == CALCULATION_FAILED) &&
            !showDetails

    fun getRiskLevelHeadline(c: Context): String {
        if (isTracingOff()) {
            return c.getString(R.string.risk_card_no_calculation_possible_headline)
        }
        return when (riskState) {
            INCREASED_RISK -> R.string.risk_card_increased_risk_headline
            LOW_RISK -> R.string.risk_card_low_risk_headline
            CALCULATION_FAILED -> R.string.risk_card_check_failed_no_internet_headline
        }.let { c.getString(it) }
    }


    fun isTracingInProgress(): Boolean = tracingProgress != TracingProgress.Idle

    fun getUpdateButtonColor(c: Context): Int = when (riskState) {
        INCREASED_RISK, LOW_RISK -> R.color.colorStableLight
        else -> R.color.colorAccentTintButton
    }.let { c.getColor(it) }

    fun getUpdateButtonTextColor(c: Context): Int = when (riskState) {
        INCREASED_RISK, LOW_RISK -> R.color.colorTextPrimary1Stable
        else -> R.color.colorTextPrimary1InvertedStable
    }.let { c.getColor(it) }
}
