package de.rki.coronawarnapp.ui.tracing.common

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

class BaseTracingStateTest : BaseTest() {

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
        riskLevelScore: Int = 0,
        isRefreshing: Boolean = false,
        riskLevelLastSuccessfulCalculation: Int = 0,
        matchedKeyCount: Int = 0,
        daysSinceLastExposure: Int = 0,
        activeTracingDaysInRetentionPeriod: Long = 0,
        lastTimeDiagnosisKeysFetched: Date? = mockk(),
        isBackgroundJobEnabled: Boolean = false,
        isManualKeyRetrievalEnabled: Boolean = false,
        manualKeyRetrievalTime: Long = 0L,
        showDetails: Boolean = false
    ) = object : BaseTracingState() {
        override val tracingStatus: GeneralTracingStatus.Status = tracingStatus
        override val riskLevelScore: Int = riskLevelScore
        override val isRefreshing: Boolean = isRefreshing
        override val riskLevelLastSuccessfulCalculation: Int = riskLevelLastSuccessfulCalculation
        override val matchedKeyCount: Int = matchedKeyCount
        override val daysSinceLastExposure: Int = daysSinceLastExposure
        override val activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod
        override val lastTimeDiagnosisKeysFetched: Date? = lastTimeDiagnosisKeysFetched
        override val isBackgroundJobEnabled: Boolean = isBackgroundJobEnabled
        override val showDetails: Boolean = showDetails
        override val isManualKeyRetrievalEnabled: Boolean = isManualKeyRetrievalEnabled
        override val manualKeyRetrievalTime: Long = manualKeyRetrievalTime
    }

    @Test
    fun `risk card shape`() {
        TODO("getRiskShape")
//    private fun formatRiskShapeBase(bShowDetails: Boolean) {
//        every { context.getDrawable(any()) } returns drawable
//
//        val result = formatRiskShape(showDetails = bShowDetails)
//        assertThat(
//            result, `is`(drawable)
//        )
//    }
//
//    @Test
//    fun formatRiskShape() {
//        formatRiskShapeBase(bShowDetails = true)
//        formatRiskShapeBase(bShowDetails = false)
//    }
    }

    @Test
    fun `risk color`() {
        TODO("getRiskColor")
//        private fun formatRiskColorBase(iRiskLevelScore: Int?, iValue: Int) {
//        every { context.getColor(R.color.colorSemanticNeutralRisk) } returns R.color.colorSemanticNeutralRisk
//        every { context.getColor(R.color.colorSemanticHighRisk) } returns R.color.colorSemanticHighRisk
//        every { context.getColor(R.color.colorSemanticUnknownRisk) } returns R.color.colorSemanticUnknownRisk
//        every { context.getColor(R.color.colorSemanticLowRisk) } returns R.color.colorSemanticLowRisk
//
//        val result = formatRiskColor(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//    @Test
//    fun formatRiskColor() {
//        formatRiskColorBase(iRiskLevelScore = null, iValue = R.color.colorSemanticNeutralRisk)
//        formatRiskColorBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iValue = R.color.colorSemanticHighRisk
//        )
//        formatRiskColorBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iValue = R.color.colorSemanticUnknownRisk
//        )
//        formatRiskColorBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iValue = R.color.colorSemanticUnknownRisk
//        )
//        formatRiskColorBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iValue = R.color.colorSemanticLowRisk
//        )
//        formatRiskColorBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iValue = R.color.colorSemanticNeutralRisk
//        )
//    }
    }

    @Test
    fun `risk tracing off level`() {
        TODO("isTracingOffRiskLevel")
    }

    @Test
    fun `text color`() {
        TODO("getStableTextColor")
//            private fun formatStableTextColorBase(iRiskLevelScore: Int?) {
//        every { context.getColor(any()) } returns R.color.colorStableLight
//
//        val result = formatStableTextColor(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(R.color.colorStableLight)
//        )
//    }
//
//    @Test
//    fun formatStableTextColor() {
//        formatStableTextColorBase(iRiskLevelScore = null)
//        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
//        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
//        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
//        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
//        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
//    }
    }

    @Test
    fun `update button text`() {
        TODO("getUpdateButtonText")
//           private fun formatButtonUpdateTextBase(lTime: Long, sValue: String) {
//        val result = formatButtonUpdateText(time = lTime)
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//    @Test
//    fun formatButtonUpdateText() {
//        formatButtonUpdateTextBase(
//            lTime = 0,
//            sValue = context.getString(R.string.risk_card_button_update)
//        )
//        formatButtonUpdateTextBase(
//            lTime = 604800,
//            sValue = context.getString(R.string.risk_card_button_cooldown)
//        )
//    }
    }

    @Test
    fun `update button enabled state`() {
        TODO("isUpdateButtonEnabled")
//            private fun formatButtonUpdateEnabledBase(bEnabled: Boolean?, bValue: Boolean) {
//        val result = formatButtonUpdateEnabled(enabled = bEnabled)
//        assertThat(
//            result, `is`(bValue)
//        )
//    }
//
//    @Test
//    fun formatButtonUpdateEnabled() {
//        formatButtonUpdateEnabledBase(bEnabled = true, bValue = true)
//        formatButtonUpdateEnabledBase(bEnabled = false, bValue = false)
//    }
    }
}
