package de.rki.coronawarnapp.ui.tracing.details

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.TracingStatus
import de.rki.coronawarnapp.ui.tracing.common.BaseTracingState
import java.util.Date

data class TracingDetailsState(
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
    override val manualKeyRetrievalTime: Long,
    val isInformationBodyNoticeVisible: Boolean, // tracingViewModel.informationBodyNoticeVisibility
    val isAdditionalInformationVisible: Boolean // tracingViewModel.additionalInformationVisibility
) : BaseTracingState() {

    override val showDetails: Boolean = true

    /**
     * Format the risk details include display for suggested behavior depending on risk level
     * in all cases when risk level is not increased
     */
    fun isBehaviorNormalVisible(c: Context): Boolean =
        riskLevelScore != RiskLevelConstants.INCREASED_RISK

    /**
     * Format the risk details include display for suggested behavior depending on risk level
     * Only applied in special case for increased risk
     */
    fun isBehaviorIncreasedRiskVisible(c: Context): Boolean =
        riskLevelScore == RiskLevelConstants.INCREASED_RISK

    /**
     * Format the risk details period logged card display  depending on risk level
     * applied in case of low and high risk levels
     */
    fun isBehaviorPeriodLoggedVisible(c: Context): Boolean =
        riskLevelScore == RiskLevelConstants.INCREASED_RISK || riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK

    /**
     * Format the risk details include display for suggested behavior depending on risk level
     * Only applied in special case for low level risk
     */
    fun isBehaviorLowLevelRiskVisible(c: Context): Boolean =
        riskLevelScore == RiskLevelConstants.LOW_LEVEL_RISK

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
                    if (count > 0)
                        R.string.risk_details_information_body_low_risk_with_encounter
                    else
                        R.string.risk_details_information_body_low_risk
                )
            RiskLevelConstants.UNKNOWN_RISK_INITIAL ->
                c.getString(R.string.risk_details_information_body_unknown_risk)
            else -> ""
        }
    }

    /**
     * Formats the risk details text display for each risk level for the body notice
     */
    fun getRiskDetailsRiskLevelBodyNotice(c: Context): String {
        val resources = c.resources
        return when (riskLevelScore) {
            RiskLevelConstants.INCREASED_RISK ->
                resources.getString(R.string.risk_details_information_body_notice_increased)
            RiskLevelConstants.LOW_LEVEL_RISK ->
                resources.getString(R.string.risk_details_information_body_notice_low)
            else -> c.getString(R.string.risk_details_information_body_notice)
        }
    }

    /**
     * Formats the risk details button display for enable tracing depending on risk level
     */
    fun areRiskDetailsButtonsVisible(c: Context): Boolean =
        isRiskDetailsEnableTracingButtonVisible(c) || isRiskDetailsUpdateButtonVisible(c)

    /**
     * Formats the risk details button display for enable tracing depending on risk level
     */
    fun isRiskDetailsEnableTracingButtonVisible(c: Context): Boolean = isTracingOffRiskLevel()

    /**
     * Formats the risk details button display for manual updates depending on risk level and
     * background task setting
     */
    fun isRiskDetailsUpdateButtonVisible(c: Context): Boolean =
        isTracingOffRiskLevel() && !isBackgroundJobEnabled

    /**
     * Formats the risk logged period card text display of tracing active duration in days depending on risk level
     * Displayed in case riskLevel is High and Low level
     */
    fun getRiskActiveTracingDaysInRetentionPeriodLogged(c: Context): String {
        return c.getString(
            R.string.risk_details_information_body_period_logged_assessment
        ).format(activeTracingDaysInRetentionPeriod)
    }
}
