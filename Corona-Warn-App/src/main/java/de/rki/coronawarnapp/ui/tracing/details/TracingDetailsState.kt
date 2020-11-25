package de.rki.coronawarnapp.ui.tracing.details

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.ui.tracing.common.BaseTracingState

data class TracingDetailsState(
    override val tracingStatus: GeneralTracingStatus.Status,
    override val riskLevelScore: Int,
    override val tracingProgress: TracingProgress,
    val matchedKeyCount: Int,
    val activeTracingDaysInRetentionPeriod: Long,
    override val isManualKeyRetrievalEnabled: Boolean,
    val isInformationBodyNoticeVisible: Boolean,
    val isAdditionalInformationVisible: Boolean,
    val daysSinceLastExposure: Int
) : BaseTracingState() {

    override val showDetails: Boolean = true

    /**
     * Format the risk details include display for suggested behavior depending on risk level
     * in all cases when risk level is not increased
     */
    fun isBehaviorNormalVisible(): Boolean =
        riskLevelScore != RiskLevelConstants.INCREASED_RISK

    /**
     * Format the risk details include display for suggested behavior depending on risk level
     * Only applied in special case for increased risk
     */
    fun isBehaviorIncreasedRiskVisible(): Boolean =
        riskLevelScore == RiskLevelConstants.INCREASED_RISK

    /**
     * Format the risk details period logged card display  depending on risk level
     * applied in case of low and high risk levels
     */
    fun isBehaviorPeriodLoggedVisible(): Boolean =
        riskLevelScore == RiskLevelConstants.INCREASED_RISK || riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK

    /**
     * Format the risk details include display for suggested behavior depending on risk level
     * Only applied in special case for low level risk
     */
    fun isBehaviorLowLevelRiskVisible(): Boolean =
        riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK && matchedKeyCount > 0

    /**
     * Formats the risk details text display for each risk level
     */
    fun getRiskDetailsRiskLevelBody(c: Context): String {
        val resources = c.resources
        val days = daysSinceLastExposure
        val count = matchedKeyCount
        return when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK ->
                resources.getQuantityString(
                    R.plurals.risk_details_information_body_increased_risk,
                    days,
                    days
                )
            RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS ->
                c.getString(R.string.risk_details_information_body_outdated_risk)
            RiskLevelConstants.LOW_LEVEL_RISK ->
                c.getString(
                    if (count > 0) R.string.risk_details_information_body_low_risk_with_encounter
                    else R.string.risk_details_information_body_low_risk
                )
            else -> ""
        }
    }

    /**
     * Formats the risk details text display for each risk level for the body notice
     */
    fun getRiskDetailsRiskLevelBodyNotice(c: Context): String = when (riskLevelScore) {
        RiskLevelConstants.INCREASED_RISK -> R.string.risk_details_information_body_notice_increased
        else -> R.string.risk_details_information_body_notice
    }.let { c.getString(it) }

    /**
     * Formats the risk details button display for enable tracing depending on risk level
     */
    fun areRiskDetailsButtonsVisible(): Boolean =
        isRiskDetailsEnableTracingButtonVisible() || isRiskDetailsUpdateButtonVisible()

    /**
     * Formats the risk details button display for enable tracing depending on risk level
     */
    fun isRiskDetailsEnableTracingButtonVisible(): Boolean = isTracingOffRiskLevel()

    /**
     * Formats the risk details button display for manual updates depending on risk level and
     * background task setting
     */
    fun isRiskDetailsUpdateButtonVisible(): Boolean = !isTracingOffRiskLevel() && isManualKeyRetrievalEnabled

    /**
     * Formats the risk logged period card text display of tracing active duration in days depending on risk level
     * Displayed in case riskLevel is High and Low level
     */
    fun getRiskActiveTracingDaysInRetentionPeriodLogged(c: Context): String = c.getString(
        R.string.risk_details_information_body_period_logged_assessment
    ).format(activeTracingDaysInRetentionPeriod)

    fun getBehaviorIcon(context: Context) = when {
        tracingStatus != GeneralTracingStatus.Status.TRACING_ACTIVE -> R.color.colorTextSemanticNeutral
        riskLevelScore == RiskLevelConstants.INCREASED_RISK ||
            riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorStableLight
        else -> R.color.colorTextSemanticNeutral
    }.let { context.getColor(it) }

    /**
     * Formats the risk details suggested behavior icon background color depending on risk level
     *
     * @param riskLevelScore
     * @return
     */
    fun getBehaviorIconBackground(context: Context) = when {
        tracingStatus != GeneralTracingStatus.Status.TRACING_ACTIVE -> R.color.colorSurface2
        riskLevelScore == RiskLevelConstants.INCREASED_RISK -> R.color.colorSemanticHighRisk
            riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK -> R.color.colorSemanticLowRisk
        else -> R.color.colorSurface2
    }.let { context.getColor(it) }
}
