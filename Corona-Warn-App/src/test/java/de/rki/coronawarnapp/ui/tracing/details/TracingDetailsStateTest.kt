package de.rki.coronawarnapp.ui.tracing.details

import android.content.Context
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Date

class TracingDetailsStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(
        tracingStatus: GeneralTracingStatus.Status = mockk(),
        riskLevel: Int = 0,
        isRefreshing: Boolean = false,
        riskLevelLastSuccessfulCalculation: Int = 0,
        matchedKeyCount: Int = 0,
        daysSinceLastExposure: Int = 0,
        activeTracingDaysInRetentionPeriod: Long = 0,
        lastTimeDiagnosisKeysFetched: Date? = mockk(),
        isBackgroundJobEnabled: Boolean = false,
        isManualKeyRetrievalEnabled: Boolean = false,
        manualKeyRetrievalTime: Long = 0L,
        isInformationBodyNoticeVisible: Boolean = false,
        isAdditionalInformationVisible: Boolean = false
    ) = TracingDetailsState(
        tracingStatus = tracingStatus,
        riskLevelScore = riskLevel,
        isRefreshing = isRefreshing,
        riskLevelLastSuccessfulCalculation = riskLevelLastSuccessfulCalculation,
        matchedKeyCount = matchedKeyCount,
        daysSinceLastExposure = daysSinceLastExposure,
        activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
        isBackgroundJobEnabled = isBackgroundJobEnabled,
        isManualKeyRetrievalEnabled = isManualKeyRetrievalEnabled,
        manualKeyRetrievalTime = manualKeyRetrievalTime,
        isInformationBodyNoticeVisible = isInformationBodyNoticeVisible,
        isAdditionalInformationVisible = isAdditionalInformationVisible
    )

    @Test
    fun `normal behavior visibility`() {
        TODO("isBehaviorNormalVisible")
//            private fun formatVisibilityBehaviorBase(iRiskLevelScore: Int?, iValue: Int) {
//        val result = formatVisibilityBehavior(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//    @Test
//    fun formatVisibilityBehavior() {
//        formatVisibilityBehaviorBase(iRiskLevelScore = null, iValue = View.VISIBLE)
//        formatVisibilityBehaviorBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iValue = View.GONE
//        )
//        formatVisibilityBehaviorBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iValue = View.VISIBLE
//        )
//        formatVisibilityBehaviorBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iValue = View.VISIBLE
//        )
//        formatVisibilityBehaviorBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iValue = View.VISIBLE
//        )
//        formatVisibilityBehaviorBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iValue = View.VISIBLE
//        )
//    }
    }

    @Test
    fun `increased risk visibility`() {
        TODO("isBehaviorIncreasedRiskVisible")
//            private fun formatVisibilityBehaviorIncreasedRiskBase(iRiskLevelScore: Int?, iValue: Int) {
//        val result = formatVisibilityBehaviorIncreasedRisk(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//
//    @Test
//    fun formatVisibilityBehaviorIncreasedRisk() {
//        formatVisibilityBehaviorIncreasedRiskBase(iRiskLevelScore = null, iValue = View.GONE)
//        formatVisibilityBehaviorIncreasedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iValue = View.VISIBLE
//        )
//        formatVisibilityBehaviorIncreasedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iValue = View.GONE
//        )
//        formatVisibilityBehaviorIncreasedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iValue = View.GONE
//        )
//        formatVisibilityBehaviorIncreasedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iValue = View.GONE
//        )
//        formatVisibilityBehaviorIncreasedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iValue = View.GONE
//        )
//    }
    }

    @Test
    fun `logged period card visibility`() {
        TODO("isBehaviorPeriodLoggedVisible")
    }

    @Test
    fun `low level risk visibility`() {
        TODO("isBehaviorLowLevelRiskVisible")
    }

    @Test
    fun `risk details body text`() {
        TODO("getRiskDetailsRiskLevelBody")
//        private fun formatRiskDetailsRiskLevelBodyBase(
//        iRiskLevelScore: Int?,
//        iDaysSinceLastExposure: Int?,
//        iMatchedKeysCount: Int?,
//        sValue: String
//    ) {
//        every { context.getString(R.string.risk_details_information_body_outdated_risk) } returns R.string.risk_details_information_body_outdated_risk.toString()
//        every { context.getString(R.string.risk_details_information_body_low_risk) } returns R.string.risk_details_information_body_low_risk.toString()
//        every { context.getString(R.string.risk_details_information_body_unknown_risk) } returns R.string.risk_details_information_body_unknown_risk.toString()
//
//        val result = formatRiskDetailsRiskLevelBody(
//            riskLevelScore = iRiskLevelScore,
//            daysSinceLastExposure = iDaysSinceLastExposure,
//            matchedKeysCount = iMatchedKeysCount
//        )
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//
//    @Test
//    fun formatRiskDetailsRiskLevelBody() {
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = null,
//            iDaysSinceLastExposure = 0,
//            iMatchedKeysCount = 0,
//            sValue = ""
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iDaysSinceLastExposure = 1,
//            iMatchedKeysCount = 0,
//            sValue = resources.getQuantityString(
//                R.plurals.risk_details_information_body_increased_risk,
//                1,
//                1
//            )
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iDaysSinceLastExposure = 1,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_details_information_body_outdated_risk)
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iDaysSinceLastExposure = 1,
//            iMatchedKeysCount = 0,
//            sValue = ""
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iDaysSinceLastExposure = 1,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_details_information_body_low_risk)
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iDaysSinceLastExposure = 1,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_details_information_body_unknown_risk)
//        )
//
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = null, iDaysSinceLastExposure = null,
//            iMatchedKeysCount = 0, sValue = ""
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iDaysSinceLastExposure = null,
//            iMatchedKeysCount = 0,
//            sValue = resources.getQuantityString(
//                R.plurals.risk_details_information_body_increased_risk,
//                0,
//                0
//            )
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iDaysSinceLastExposure = null,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_details_information_body_outdated_risk)
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iDaysSinceLastExposure = null,
//            iMatchedKeysCount = 0,
//            sValue = ""
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iDaysSinceLastExposure = null,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_details_information_body_low_risk)
//        )
//        formatRiskDetailsRiskLevelBodyBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iDaysSinceLastExposure = null,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_details_information_body_unknown_risk)
//        )
//    }
    }

    @Test
    fun `riskdetails body notice`() {
        TODO("getRiskDetailsRiskLevelBodyNotice")
    }

    @Test
    fun `risk details buttons visibility`() {
        TODO("areRiskDetailsButtonsVisible")
//        private fun formatRiskDetailsButtonVisibilityBase(
//        iRiskLevelScore: Int?,
//        bIsBackgroundJobEnabled: Boolean?,
//        iValue: Int
//    ) {
//        val result = formatRiskDetailsButtonVisibility(
//            riskLevelScore = iRiskLevelScore,
//            isBackgroundJobEnabled = bIsBackgroundJobEnabled
//        )
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//    @Test
//    fun formatRiskDetailsButtonVisibility() {
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//    }
    }

    @Test
    fun `enable tracing button visibility`() {
        TODO("isRiskDetailsEnableTracingButtonVisible")
//            private fun formatRiskDetailsButtonEnableTracingVisibilityBase(
//        iRiskLevelScore: Int?,
//        iValue: Int
//    ) {
//        val result =
//            formatRiskDetailsButtonEnableTracingVisibility(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//    @Test
//    fun formatRiskDetailsButtonEnableTracingVisibility() {
//        formatRiskDetailsButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = null,
//            iValue = View.GONE
//        )
//        formatRiskDetailsButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iValue = View.GONE
//        )
//        formatRiskDetailsButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iValue = View.VISIBLE
//        )
//        formatRiskDetailsButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iValue = View.GONE
//        )
//        formatRiskDetailsButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iValue = View.GONE
//        )
//    }
    }

    @Test
    fun `risk details update button visibility`() {
        TODO("isRiskDetailsUpdateButtonVisible")
//  private fun formatDetailsButtonUpdateVisibilityBase(
//        iRiskLevelScore: Int?,
//        bIsBackgroundJobEnabled: Boolean?,
//        iValue: Int
//    ) {
//        val result = formatDetailsButtonUpdateVisibility(
//            riskLevelScore = iRiskLevelScore,
//            isBackgroundJobEnabled = bIsBackgroundJobEnabled
//        )
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//    @Test
//    fun formatDetailsButtonUpdateVisibility() {
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = true,
//            iValue = View.GONE
//        )
//
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.GONE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//        formatDetailsButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = false,
//            iValue = View.VISIBLE
//        )
//    }
    }

    @Test
    fun `update button visibility`() {
        TODO("showUpdateButton")
//        private fun formatButtonUpdateVisibilityBase(
//        iRiskLevelScore: Int?,
//        bIsBackgroundJobEnabled: Boolean?,
//        bShowDetails: Boolean?,
//        iValue: Int
//    ) {
//        val result = formatButtonUpdateVisibility(
//            riskLevelScore = iRiskLevelScore,
//            isBackgroundJobEnabled = bIsBackgroundJobEnabled,
//            showDetails = bShowDetails
//        )
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//
//    @Test
//    fun formatButtonUpdateVisibility() {
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//    }
    }

    @Test
    fun `format active tracing days in retention`() {
        TODO("getRiskActiveTracingDaysInRetentionPeriodLogged")
    }
}
