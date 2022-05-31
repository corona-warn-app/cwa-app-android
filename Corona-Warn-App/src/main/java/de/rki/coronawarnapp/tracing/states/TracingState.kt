package de.rki.coronawarnapp.tracing.states

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.ColorInt
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate

sealed class TracingState {
    abstract val riskState: RiskState
    abstract val isInDetailsMode: Boolean

    internal fun formatRelativeDateTimeString(context: Context, date: Instant): CharSequence? =
        DateUtils.getRelativeDateTimeString(
            context,
            date.toEpochMilli(),
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
    val lastEncounterAt: LocalDate?,
    val allowManualUpdate: Boolean,
    val daysWithEncounters: Int
) : TracingState() {

    val showUpdateButton: Boolean = allowManualUpdate && !isInDetailsMode

    fun getTimeFetched(context: Context): String = if (lastExposureDetectionTime != null) {
        context.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(context, lastExposureDetectionTime)
        )
    } else {
        context.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getRiskContactBody(context: Context): String? = if (daysWithEncounters == 0) {
        // LEGACY MIGRATION CASE FROM 1.7.x -> 1.8.x ('days with encounter' doesn't exit in 1.7.x)
        // see RiskLevelResultMigrator.kt
        null
    } else {
        context.resources.getQuantityString(
            R.plurals.risk_card_high_risk_encounter_days_body,
            daysWithEncounters,
            daysWithEncounters
        )
    }

    fun getRiskContactBodyDescription(context: Context): String = if (daysWithEncounters == 0) {
        ""
    } else {
        context.resources.getQuantityString(
            R.plurals.risk_card_high_risk_encounter_days_body_description,
            daysWithEncounters,
            daysWithEncounters
        )
    }

    fun getRiskContactLast(context: Context): String? {
        if (lastEncounterAt == null) return null
        // caution! lastEncounterAt is null after migration from 1.7.x -> 1.8.x
        // see RiskLevelResultMigrator.kt

        val stringRes = if (daysWithEncounters == 1) {
            R.string.risk_card_high_risk_most_recent_body_encounter_on_single_day
        } else {
            R.string.risk_card_high_risk_most_recent_body_encounters_on_more_than_one_day
        }

        return context.getString(
            stringRes,
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(lastEncounterAt)
        )
    }
}

// tracing_content_low_view
data class LowRisk(
    override val riskState: RiskState,
    override val isInDetailsMode: Boolean,
    val lastExposureDetectionTime: Instant?,
    val lastEncounterAt: LocalDate?,
    val allowManualUpdate: Boolean,
    val daysWithEncounters: Int,
    val daysSinceInstallation: Int
) : TracingState() {

    val showUpdateButton: Boolean = allowManualUpdate && !isInDetailsMode

    fun getTimeFetched(context: Context): String = if (lastExposureDetectionTime != null) {
        context.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(context, lastExposureDetectionTime)
        )
    } else {
        context.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getRiskContactBody(context: Context): String = if (daysWithEncounters == 0) {
        // caution! is 0 after migration from 1.7.x -> 1.8.x
        // see RiskLevelResultMigrator.kt
        context.getString(R.string.risk_card_low_risk_no_encounters_body)
    } else {
        context.resources.getQuantityString(
            R.plurals.risk_card_low_risk_encounter_days_body,
            daysWithEncounters,
            daysWithEncounters
        )
    }

    fun getRiskContactBodyDescription(context: Context): String = if (daysWithEncounters == 0) {
        context.getString(R.string.risk_card_low_risk_no_encounters_body)
    } else {
        context.resources.getQuantityString(
            R.plurals.risk_card_low_risk_encounter_days_body_description,
            daysWithEncounters,
            daysWithEncounters
        )
    }

    fun getDaysSinceInstall(context: Context): String =
        when (daysSinceInstallation) {
            0 -> context.getString(R.string.risk_card_body_installation_today)
            1 -> context.getString(R.string.risk_card_body_installation_yesterday)
            else -> context.getString(R.string.risk_card_body_days_since_installation).format(daysSinceInstallation)
        }

    fun appInstalledForOverTwoWeeks(): Boolean = daysSinceInstallation < 14 && lastEncounterAt == null

    fun getRiskContactLast(context: Context): String? {
        if (lastEncounterAt == null) return null
        // caution! lastEncounterAt is null after migration from 1.7.x -> 1.8.x
        // see RiskLevelResultMigrator.kt

        val stringRes = if (daysWithEncounters == 1) {
            R.string.risk_card_low_risk_most_recent_body_encounter_on_single_day
        } else {
            R.string.risk_card_low_risk_most_recent_body_encounters_on_more_than_one_day
        }

        return context.getString(
            stringRes,
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(lastEncounterAt)
        )
    }

    fun isGoneOnContentLowView(context: Context) = getRiskContactLast(context) != null && !isInDetailsMode
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

    fun getLastRiskState(context: Context): String {
        val argumentValue = context.getString(
            when (riskState) {
                RiskState.INCREASED_RISK -> R.string.risk_card_increased_risk_headline
                RiskState.LOW_RISK -> R.string.risk_card_low_risk_headline
                RiskState.CALCULATION_FAILED -> R.string.risk_card_check_failed_no_internet_headline
            }
        )
        return context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk).format(argumentValue)
    }
}

// tracing_content_disabled_view
data class TracingDisabled(
    override val riskState: RiskState, // Here it's the latest successful
    override val isInDetailsMode: Boolean,
    val lastExposureDetectionTime: Instant?
) : TracingState() {

    val showEnableTracingButton: Boolean = !isInDetailsMode

    fun getTimeFetched(context: Context): String = if (lastExposureDetectionTime != null) {
        context.getString(
            R.string.risk_card_body_time_fetched,
            formatRelativeDateTimeString(context, lastExposureDetectionTime)
        )
    } else {
        context.getString(R.string.risk_card_body_not_yet_fetched)
    }

    fun getLastRiskState(context: Context): String {
        val argumentValue = context.getString(
            when (riskState) {
                RiskState.INCREASED_RISK -> R.string.risk_card_increased_risk_headline
                RiskState.LOW_RISK -> R.string.risk_card_low_risk_headline
                RiskState.CALCULATION_FAILED -> R.string.risk_card_check_failed_no_internet_headline
            }
        )
        return context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk).format(argumentValue)
    }
}

data class TracingInProgress(
    override val riskState: RiskState,
    override val isInDetailsMode: Boolean,
    val tracingProgress: TracingProgress
) : TracingState() {

    fun getProgressCardHeadline(context: Context): String = when (tracingProgress) {
        TracingProgress.Downloading -> R.string.risk_card_progress_download_headline
        TracingProgress.IsCalculating -> R.string.risk_card_progress_calculation_headline
        TracingProgress.Idle -> null
    }?.let { context.getString(it) } ?: ""

    fun getProgressCardBody(context: Context): String = when (tracingProgress) {
        TracingProgress.Downloading -> R.string.risk_card_progress_download_body
        TracingProgress.IsCalculating -> R.string.risk_card_progress_calculation_body
        TracingProgress.Idle -> null
    }?.let { context.getString(it) } ?: ""

    /**
     * Formats the risk card icon color depending on risk level
     * This special handling is required due to light / dark mode differences and switches
     * between colored / light / dark background
     */
    fun getStableIconColor(context: Context): Int = when (riskState) {
        RiskState.INCREASED_RISK, RiskState.LOW_RISK -> R.color.colorStableLight
        else -> R.color.colorTextSemanticNeutral
    }.let { context.getColorCompat(it) }

    fun getStableTextColor(context: Context): Int = when (riskState) {
        RiskState.INCREASED_RISK, RiskState.LOW_RISK -> R.color.colorTextPrimary1InvertedStable
        else -> R.color.colorOnPrimary
    }.let { context.getColorCompat(it) }

    @ColorInt
    fun getContainerColor(context: Context): Int = when (riskState) {
        RiskState.INCREASED_RISK -> R.color.colorSemanticHighRisk
        RiskState.LOW_RISK -> R.color.colorSemanticLowRisk
        RiskState.CALCULATION_FAILED -> R.color.colorSemanticUnknownRisk
    }.let { context.getColorCompat(it) }
}
