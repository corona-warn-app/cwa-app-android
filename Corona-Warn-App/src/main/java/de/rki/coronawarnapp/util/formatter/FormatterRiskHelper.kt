@file:JvmName("FormatterRiskHelper")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToHMS
import java.util.Date

/*Texter*/
/** General Information
 * Risk card element visibility is handled through text formatter whenever applicable
 * Therefore the logic for visibility is only defined once
 */

/**
 * Helper function to determine if tracing is active depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
private fun isTracingOffRiskLevel(riskLevelScore: Int?): Boolean {
    return when (riskLevelScore) {
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> true
        else -> false
    }
}

/**
 * Formats the risk card headline depending on risk level
 * Special case of a running update is caught
 *
 * @param riskLevelScore
 * @param isRefreshing
 * @return
 */
fun formatRiskLevelHeadline(riskLevelScore: Int?, isRefreshing: Boolean?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (isRefreshing == true) {
        appContext.getString(R.string.risk_card_loading_headline)
    } else {
        when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK -> appContext.getString(R.string.risk_card_increased_risk_headline)
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ->
                appContext.getString(R.string.risk_card_outdated_risk_headline)
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
                appContext.getString(R.string.risk_card_no_calculation_possible_headline)
            RiskLevelConstants.LOW_LEVEL_RISK -> appContext.getString(R.string.risk_card_low_risk_headline)
            RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
                appContext.getString(R.string.risk_card_unknown_risk_headline)
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ->
                appContext.getString(R.string.risk_card_unknown_risk_headline)
            else -> ""
        }
    }
}

/**
 * Formats the risk card text display depending on risk level
 * for general information when no definite risk level
 * can be calculated
 *
 * @param riskLevelScore
 * @return
 */
fun formatRiskBody(riskLevelScore: Int?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> appContext.getString(R.string.risk_card_outdated_risk_body)
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
            appContext.getString(R.string.risk_card_body_tracing_off)
        RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
            appContext.getString(R.string.risk_card_unknown_risk_body)
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL ->
            appContext.getString(R.string.risk_card_outdated_manual_risk_body)
        else -> ""
    }
}

/**
 * Formats the risk card text display of last persisted risk level
 * only in the special case where tracing is turned off and
 * the persisted risk level is of importance
 *
 * @param riskLevelScore
 * @param riskLevelScoreLastSuccessfulCalculated
 * @return
 */
fun formatRiskSavedRisk(
    riskLevelScore: Int?,
    riskLevelScoreLastSuccessfulCalculated: Int?
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (
        riskLevelScore == RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ||
        riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ||
        riskLevelScore == RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
    ) {
        when (riskLevelScoreLastSuccessfulCalculated) {
            RiskLevelConstants.LOW_LEVEL_RISK,
            RiskLevelConstants.INCREASED_RISK,
            RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
                appContext.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(formatRiskLevelHeadline(riskLevelScoreLastSuccessfulCalculated, false))
            else -> ""
        }
    } else {
        ""
    }
}

/**
 * Formats the risk card text display of infected contacts recognized
 *
 * @param riskLevelScore
 * @param matchedKeysCount
 * @return
 */
fun formatRiskContact(riskLevelScore: Int?, matchedKeysCount: Int?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    val resources = appContext.resources
    val contacts = matchedKeysCount ?: 0
    return when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK,
        RiskLevelConstants.LOW_LEVEL_RISK -> {
            if (matchedKeysCount == 0) {
                appContext.getString(R.string.risk_card_body_contact)
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
 * Formats the risk card text display of time since the last infected contact was recognized
 * only in the special case of increased risk as a positive contact is a
 * prerequisite for increased risk
 *
 * @param riskLevelScore
 * @param daysSinceLastExposure
 * @return
 */
fun formatRiskContactLast(riskLevelScore: Int?, daysSinceLastExposure: Int?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    val resources = appContext.resources
    val days = daysSinceLastExposure ?: 0
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
 *
 * @param riskLevelScore
 * @param showDetails
 * @param activeTracingDaysInRetentionPeriod
 * @return
 */
fun formatRiskActiveTracingDaysInRetentionPeriod(
    riskLevelScore: Int?,
    showDetails: Boolean,
    activeTracingDaysInRetentionPeriod: Long
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> {
            if (showDetails) {
                if (activeTracingDaysInRetentionPeriod < TimeVariables.getDefaultRetentionPeriodInDays()) {
                    appContext.getString(
                        R.string.risk_card_body_saved_days
                    )
                        .format(activeTracingDaysInRetentionPeriod)
                } else {
                    appContext.getString(
                        R.string.risk_card_body_saved_days_full
                    )
                }
            } else {
                ""
            }
        }
        RiskLevelConstants.LOW_LEVEL_RISK ->
            if (activeTracingDaysInRetentionPeriod < TimeVariables.getDefaultRetentionPeriodInDays()) {
                appContext.getString(
                    R.string.risk_card_body_saved_days
                )
                    .format(activeTracingDaysInRetentionPeriod)
            } else {
                appContext.getString(
                    R.string.risk_card_body_saved_days_full
                )
            }

        else -> ""
    }
}

/**
 * Formats the risk logged period card text display of tracing active duration in days depending on risk level
 * Displayed in case riskLevel is High and Low level
 *
 * @param activeTracingDaysInRetentionPeriod
 * @return
 */
fun formatRiskActiveTracingDaysInRetentionPeriodLogged(
    activeTracingDaysInRetentionPeriod: Long
): String {
    val appContext = CoronaWarnApplication.getAppContext()
        return appContext.getString(
                R.string.risk_details_information_body_period_logged_assessment)
            .format(activeTracingDaysInRetentionPeriod)
}

fun formatRelativeDateTimeString(appContext: Context, date: Date): CharSequence? =
    DateUtils.getRelativeDateTimeString(
        appContext,
        date.time,
        DateUtils.DAY_IN_MILLIS,
        DateUtils.DAY_IN_MILLIS * 2,
        0
    )

/**
 * Formats the risk card text display of the last time diagnosis keys were
 * successfully fetched from the server
 *
 * @param riskLevelScore
 * @param riskLevelScoreLastSuccessfulCalculated
 * @param lastTimeDiagnosisKeysFetched
 * @return
 */
fun formatTimeFetched(
    riskLevelScore: Int?,
    riskLevelScoreLastSuccessfulCalculated: Int?,
    lastTimeDiagnosisKeysFetched: Date?
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.LOW_LEVEL_RISK,
        RiskLevelConstants.INCREASED_RISK -> {
            if (lastTimeDiagnosisKeysFetched != null) {
                appContext.getString(
                    R.string.risk_card_body_time_fetched,
                    formatRelativeDateTimeString(appContext, lastTimeDiagnosisKeysFetched)
                )
            } else {
                appContext.getString(R.string.risk_card_body_not_yet_fetched)
            }
        }
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> {
            when (riskLevelScoreLastSuccessfulCalculated) {
                RiskLevelConstants.LOW_LEVEL_RISK,
                RiskLevelConstants.INCREASED_RISK,
                RiskLevelConstants.UNKNOWN_RISK_INITIAL -> {
                    if (lastTimeDiagnosisKeysFetched != null) {
                        appContext.getString(
                            R.string.risk_card_body_time_fetched,
                            formatRelativeDateTimeString(appContext, lastTimeDiagnosisKeysFetched)
                        )
                    } else {
                        appContext.getString(R.string.risk_card_body_not_yet_fetched)
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
 *
 * @param riskLevelScore
 * @param isBackgroundJobEnabled
 * @return
 */
fun formatNextUpdate(
    riskLevelScore: Int?,
    isBackgroundJobEnabled: Boolean?
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (isBackgroundJobEnabled != true) {
        ""
    } else {
        return when (riskLevelScore) {
            RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            RiskLevelConstants.LOW_LEVEL_RISK,
            RiskLevelConstants.INCREASED_RISK -> appContext.getString(
                R.string.risk_card_body_next_update
            )
            else -> ""
        }
    }
}

/**
 * Formats the risk card content description of time when diagnosis keys will be updated
 * from server again when applicable but appends the word button at the end for screen reader accessibility reasons
 *
 * @param riskLevelScore
 * @param isBackgroundJobEnabled
 * @return
 */
fun formatNextUpdateContentDescription(
    riskLevelScore: Int?,
    isBackgroundJobEnabled: Boolean?
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (isBackgroundJobEnabled != true) {
        ""
    } else {
        return when (riskLevelScore) {
            RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            RiskLevelConstants.LOW_LEVEL_RISK,
            RiskLevelConstants.INCREASED_RISK -> appContext.getString(
                R.string.risk_card_body_next_update
            ) + " " + appContext.getString(
                R.string.accessibility_button
            )
            else -> ""
        }
    }
}

/**
 * Formats the risk details text display for each risk level
 *
 * @param riskLevelScore
 * @param daysSinceLastExposure
 * @return
 */
fun formatRiskDetailsRiskLevelBody(riskLevelScore: Int?, daysSinceLastExposure: Int?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    val resources = appContext.resources
    val days = daysSinceLastExposure ?: 0
    return when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK ->
            resources.getQuantityString(
                R.plurals.risk_details_information_body_increased_risk,
                days,
                days
            )
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ->
            appContext.getString(R.string.risk_details_information_body_outdated_risk)
        RiskLevelConstants.LOW_LEVEL_RISK ->
            appContext.getString(R.string.risk_details_information_body_low_risk)
        RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
            appContext.getString(R.string.risk_details_information_body_unknown_risk)
        else -> ""
    }
}

/*Styler*/

/**
 * Formats the risk card colors for default and pressed states depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
fun formatRiskColorStateList(riskLevelScore: Int?): ColorStateList? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> appContext.getColorStateList(R.color.card_increased)
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> appContext.getColorStateList(R.color.card_outdated)
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF ->
            appContext.getColorStateList(R.color.card_no_calculation)
        RiskLevelConstants.LOW_LEVEL_RISK -> appContext.getColorStateList(R.color.card_low)
        else -> appContext.getColorStateList(R.color.card_unknown)
    }
}

/**
 * Formats the risk card colors for default and pressed states depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
fun formatRiskColor(riskLevelScore: Int?): Int? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> appContext.getColor(R.color.colorSemanticHighRisk)
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> appContext.getColor(R.color.colorSemanticUnknownRisk)
        RiskLevelConstants.LOW_LEVEL_RISK -> appContext.getColor(R.color.colorSemanticLowRisk)
        else -> appContext.getColor(R.color.colorSemanticNeutralRisk)
    }
}

/**
 * Formats the risk card style depending on current view
 *
 * @param showDetails
 * @return
 */
fun formatRiskShape(showDetails: Boolean): Drawable? =
    formatDrawable(showDetails, R.drawable.rectangle, R.drawable.card)

/**
 * Formats the risk card icon color depending on risk level
 * This special handling is required due to light / dark mode differences and switches
 * between colored / light / dark background
 *
 * @param riskLevelScore
 * @return
 */
fun formatStableIconColor(riskLevelScore: Int?): Int =
    formatColor(
        !isTracingOffRiskLevel(riskLevelScore),
        R.color.colorStableLight,
        R.color.colorTextSemanticNeutral
    )

/**
 * Formats the risk card text color depending on risk level
 * This special handling is required due to light / dark mode differences and switches
 * between colored / light / dark background
 *
 * @param riskLevelScore
 * @return
 */
fun formatStableTextColor(riskLevelScore: Int?): Int =
    formatColor(
        !isTracingOffRiskLevel(riskLevelScore),
        R.color.colorStableLight,
        R.color.colorTextPrimary1
    )

/**
 * Formats the risk card divider color depending on risk level
 * This special handling is required due to light / dark mode differences and switches
 * between colored / light / dark background
 *
 * @param riskLevelScore
 * @return
 */
fun formatStableDividerColor(riskLevelScore: Int?): Int =
    formatColor(
        !isTracingOffRiskLevel(riskLevelScore),
        R.color.colorStableHairlineLight,
        R.color.colorStableHairlineDark
    )

/**
 * Formats the risk card icon display of infected contacts recognized
 *
 * @param riskLevelScore
 * @return
 */
fun formatRiskContactIcon(riskLevelScore: Int?): Drawable? =
    formatDrawable(
        riskLevelScore == RiskLevelConstants.INCREASED_RISK,
        R.drawable.ic_risk_card_contact_increased,
        R.drawable.ic_risk_card_contact
    )

/**
 * Formats the risk card button display for enable tracing depending on risk level and current view
 *
 * @param riskLevelScore
 * @param showDetails
 * @return
 */
fun formatButtonEnableTracingVisibility(
    riskLevelScore: Int?,
    showDetails: Boolean?
): Int = formatVisibility((isTracingOffRiskLevel(riskLevelScore) && showDetails != true))

/**
 * Formats the risk details button display for enable tracing depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
fun formatRiskDetailsButtonEnableTracingVisibility(
    riskLevelScore: Int?
): Int = formatVisibility(isTracingOffRiskLevel(riskLevelScore))

/**
 * Formats the risk details button display for enable tracing depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
fun formatRiskDetailsButtonVisibility(
    riskLevelScore: Int?,
    isBackgroundJobEnabled: Boolean?
): Int = formatVisibility(
    formatRiskDetailsButtonEnableTracingVisibility(riskLevelScore) == View.VISIBLE ||
            formatDetailsButtonUpdateVisibility(
                isBackgroundJobEnabled,
                riskLevelScore
            ) == View.VISIBLE
)

/**
 * Formats the risk card button display for manual updates depending on risk level,
 * background task setting and current view
 *
 * @param riskLevelScore
 * @param isBackgroundJobEnabled
 * @param showDetails
 * @return
 */
fun formatButtonUpdateVisibility(
    riskLevelScore: Int?,
    isBackgroundJobEnabled: Boolean?,
    showDetails: Boolean?
): Int = formatVisibility(
    (!isTracingOffRiskLevel(riskLevelScore) &&
            isBackgroundJobEnabled != true &&
            showDetails != true
            )
)

/**
 * Formats the risk details button display for manual updates depending on risk level and
 * background task setting
 *
 * @param isBackgroundJobEnabled
 * @param riskLevelScore
 * @return
 */
fun formatDetailsButtonUpdateVisibility(
    isBackgroundJobEnabled: Boolean?,
    riskLevelScore: Int?
): Int = formatVisibility(
    (
            !isTracingOffRiskLevel(riskLevelScore) &&
                    isBackgroundJobEnabled != true
            )
)

/*Behavior*/
/**
 * Format the risk details include display for suggested behavior depending on risk level
 * in all cases when risk level is not increased
 *
 * @param riskLevelScore
 * @return
 */
fun formatVisibilityBehavior(riskLevelScore: Int?): Int =
    formatVisibility(riskLevelScore != RiskLevelConstants.INCREASED_RISK)

/**
 * Format the risk details include display for suggested behavior depending on risk level
 * Only applied in special case for increased risk
 *
 * @param riskLevelScore
 * @return
 */
fun formatVisibilityBehaviorIncreasedRisk(riskLevelScore: Int?): Int =
    formatVisibility(riskLevelScore == RiskLevelConstants.INCREASED_RISK)

/**
 * Format the risk details period logged card display  depending on risk level
 * applied in case of low and high risk levels
 *
 * @param riskLevelScore
 * @return
 */
fun formatVisibilityBehaviorPeriodLogged(riskLevelScore: Int?): Int =
    formatVisibility(
        riskLevelScore == RiskLevelConstants.INCREASED_RISK ||
                riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK)

/**
 * Formats the risk details suggested behavior icon color depending on risk level
 * This special handling is required due to light / dark mode differences and switches
 * between colored / light / dark background
 *
 * @param riskLevelScore
 * @return
 */
fun formatBehaviorIcon(riskLevelScore: Int?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> appContext.getColor(R.color.colorTextSemanticNeutral)
        else -> appContext.getColor(R.color.colorStableLight)
    }
}

/**
 * Formats the risk details suggested behavior icon background color depending on risk level
 *
 * @param riskLevelScore
 * @return
 */
fun formatBehaviorIconBackground(riskLevelScore: Int?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> appContext.getColor(R.color.colorSemanticHighRisk)
        RiskLevelConstants.LOW_LEVEL_RISK -> appContext.getColor(R.color.colorSemanticLowRisk)
        RiskLevelConstants.UNKNOWN_RISK_INITIAL -> appContext.getColor(R.color.colorSemanticNeutralRisk)
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> appContext.getColor(R.color.colorSemanticNeutralRisk)
        else -> appContext.getColor(R.color.colorSurface2)
    }
}

fun formatButtonUpdateEnabled(enabled: Boolean?): Boolean {
    return enabled ?: true
}

/**
 * Change the manual update button text according to current timer
 *
 * @param time
 * @return String
 */
fun formatButtonUpdateText(
    time: Long
): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return if (time <= 0) {
        appContext.getString(R.string.risk_card_button_update)
    } else {
        val hmsCooldownTime = time.millisecondsToHMS()
        appContext.getString(R.string.risk_card_button_cooldown).format(hmsCooldownTime)
    }
}
