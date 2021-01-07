package de.rki.coronawarnapp.tracing.states

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

sealed class TracingState {
    abstract val riskState: RiskState
    abstract val isInDetailsMode: Boolean

    internal fun formatRelativeDateTimeString(c: Context, date: Instant): CharSequence? =
        DateUtils.getRelativeDateTimeString(
            c,
            date.millis,
            DateUtils.DAY_IN_MILLIS,
            DateUtils.DAY_IN_MILLIS * 2,
            0
        )
}

// tracing_content_increased_view
data class IncreasedRisk(
    override val riskState: RiskState,
    override val isInDetailsMode: Boolean,
    val lastExposureDetectionTime: Instant?,
    val lastEncounterAt: Instant?,
    val allowManualUpdate: Boolean,
    val daysWithEncounters: Int,
    val activeTracingDays: Int
) : TracingState() {

    val showUpdateButton: Boolean = allowManualUpdate && !isInDetailsMode

    fun getTimeFetched(c: Context): String = if (lastExposureDetectionTime != null) {
        c.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(c, lastExposureDetectionTime)
        )
    } else {
        c.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getRiskContactBody(c: Context): String = if (daysWithEncounters == 0) {
        // LEGACY MIGRATION CASE FROM 1.7.x -> 1.8.x ('days with encounter' doesn't exit in 1.7.x)
        // see RiskLevelResultMigrator.kt
        ""
    } else {
        c.resources.getQuantityString(
            R.plurals.risk_card_high_risk_encounter_days_body,
            daysWithEncounters,
            daysWithEncounters
        )
    }

    fun getRiskActiveTracingDaysInRetentionPeriod(c: Context): String {
        if (!isInDetailsMode) return ""

        return if (activeTracingDays < TimeVariables.getDefaultRetentionPeriodInDays()) {
            c.getString(R.string.risk_card_body_saved_days).format(activeTracingDays)
        } else {
            c.getString(R.string.risk_card_body_saved_days_full)
        }
    }

    fun getRiskContactLast(c: Context): String? {
        if (lastEncounterAt == null) return null
        // caution! lastEncounterAt is null after migration from 1.7.x -> 1.8.x
        // see RiskLevelResultMigrator.kt
        return c.getString(
            R.string.risk_card_high_risk_most_recent_body,
            lastEncounterAt.toLocalDate().toString(DateTimeFormat.mediumDate())
        )
    }
}

// tracing_content_low_view
data class LowRisk(
    override val riskState: RiskState,
    override val isInDetailsMode: Boolean,
    val lastExposureDetectionTime: Instant?,
    val allowManualUpdate: Boolean,
    val daysWithEncounters: Int,
    val activeTracingDays: Int
) : TracingState() {

    val showUpdateButton: Boolean = allowManualUpdate && !isInDetailsMode

    fun getTimeFetched(c: Context): String = if (lastExposureDetectionTime != null) {
        c.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(c, lastExposureDetectionTime)
        )
    } else {
        c.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getRiskContactBody(c: Context): String = if (daysWithEncounters == 0) {
        // caution! is 0 after migration from 1.7.x -> 1.8.x
        // see RiskLevelResultMigrator.kt
        c.getString(R.string.risk_card_low_risk_no_encounters_body)
    } else {
        c.resources.getQuantityString(
            R.plurals.risk_card_low_risk_encounter_days_body,
            daysWithEncounters,
            daysWithEncounters
        )
    }

    fun getRiskActiveTracingDaysInRetentionPeriod(c: Context): String =
        if (activeTracingDays < TimeVariables.getDefaultRetentionPeriodInDays()) {
            c.getString(R.string.risk_card_body_saved_days).format(activeTracingDays)
        } else {
            c.getString(R.string.risk_card_body_saved_days_full)
        }
}

// tracing_content_failed_view
data class TracingFailed(
    override val riskState: RiskState, // Here it's the latest successful
    override val isInDetailsMode: Boolean,
    val lastExposureDetectionTime: Instant?
) : TracingState() {

    val showRestartButton: Boolean = !isInDetailsMode

    fun getTimeFetched(context: Context): String = if (lastExposureDetectionTime != null) {
        context.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(context, lastExposureDetectionTime)
        )
    } else {
        context.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getLastRiskState(c: Context): String {
        val argumentValue = c.getString(
            when (riskState) {
                RiskState.INCREASED_RISK -> R.string.risk_card_increased_risk_headline
                RiskState.LOW_RISK -> R.string.risk_card_low_risk_headline
                RiskState.CALCULATION_FAILED -> R.string.risk_card_check_failed_no_internet_headline
            }
        )
        return c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk).format(argumentValue)
    }
}

// tracing_content_disabled_view
data class TracingDisabled(
    override val riskState: RiskState, // Here it's the latest successful
    override val isInDetailsMode: Boolean,
    val lastExposureDetectionTime: Instant?
) : TracingState() {

    val showEnableTracingButton: Boolean = !isInDetailsMode

    fun getTimeFetched(c: Context): String = if (lastExposureDetectionTime != null) {
        c.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(c, lastExposureDetectionTime)
        )
    } else {
        c.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getLastRiskState(c: Context): String {
        val argumentValue = c.getString(
            when (riskState) {
                RiskState.INCREASED_RISK -> R.string.risk_card_increased_risk_headline
                RiskState.LOW_RISK -> R.string.risk_card_low_risk_headline
                RiskState.CALCULATION_FAILED -> R.string.risk_card_check_failed_no_internet_headline
            }
        )
        return c.getString(R.string.risk_card_no_calculation_possible_body_saved_risk).format(argumentValue)
    }
}

data class TracingInProgress(
    override val riskState: RiskState,
    override val isInDetailsMode: Boolean,
    val tracingProgress: TracingProgress
) : TracingState() {

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

    /**
     * Formats the risk card icon color depending on risk level
     * This special handling is required due to light / dark mode differences and switches
     * between colored / light / dark background
     */
    fun getStableIconColor(c: Context): Int = when (riskState) {
        RiskState.INCREASED_RISK, RiskState.LOW_RISK -> R.color.colorStableLight
        else -> R.color.colorTextSemanticNeutral
    }.let { c.getColorCompat(it) }

    fun getStableTextColor(c: Context): Int = when (riskState) {
        RiskState.INCREASED_RISK, RiskState.LOW_RISK -> R.color.colorTextPrimary1InvertedStable
        else -> R.color.colorTextPrimary1
    }.let { c.getColorCompat(it) }

    @ColorInt
    fun getContainerColor(c: Context): Int = when (riskState) {
        RiskState.INCREASED_RISK -> R.color.colorSemanticHighRisk
        RiskState.LOW_RISK -> R.color.colorSemanticLowRisk
        RiskState.CALCULATION_FAILED -> R.color.colorSemanticUnknownRisk
    }.let { c.getColorCompat(it) }
}
