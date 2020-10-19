package de.rki.coronawarnapp.ui.tracing.details

import android.content.Context
import android.content.res.Resources
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Date

class TracingDetailsStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context
    @MockK(relaxed = true) lateinit var resources: Resources

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.resources } returns resources
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
        matchedKeyCount: Int = 3,
        daysSinceLastExposure: Int = 2,
        activeTracingDaysInRetentionPeriod: Long = 0,
        lastTimeDiagnosisKeysFetched: Date? = mockk(),
        isBackgroundJobEnabled: Boolean = false,
        isManualKeyRetrievalEnabled: Boolean = false,
        manualKeyRetrievalTime: Long = 0L,
        isInformationBodyNoticeVisible: Boolean = false,
        isAdditionalInformationVisible: Boolean = false
    ) = TracingDetailsState(
        tracingStatus = tracingStatus,
        riskLevelScore = riskLevelScore,
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
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            isBehaviorNormalVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            isBehaviorNormalVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            isBehaviorNormalVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            isBehaviorNormalVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            isBehaviorNormalVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            isBehaviorNormalVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            isBehaviorNormalVisible(context) shouldBe true
        }
    }

    @Test
    fun `increased risk visibility`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            isBehaviorIncreasedRiskVisible(context) shouldBe false
        }
    }

    @Test
    fun `logged period card visibility`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            isBehaviorPeriodLoggedVisible(context) shouldBe false
        }
    }

    @Test
    fun `low level risk visibility`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            isBehaviorLowLevelRiskVisible(context) shouldBe false
        }
    }

    @Test
    fun `risk details body text`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_unknown_risk) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskDetailsRiskLevelBody(context) shouldBe ""
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            matchedKeyCount = 1
        ).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_low_risk_with_encounter) }
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            matchedKeyCount = 0
        ).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_low_risk) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            getRiskDetailsRiskLevelBody(context)
            verify {
                resources.getQuantityString(
                    R.plurals.risk_details_information_body_increased_risk,
                    daysSinceLastExposure, daysSinceLastExposure
                )
            }
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_outdated_risk) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            getRiskDetailsRiskLevelBody(context) shouldBe ""
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            getRiskDetailsRiskLevelBody(context) shouldBe ""
        }
    }

    @Test
    fun `riskdetails body notice`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice_low) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice_increased) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
    }

    @Test
    fun `risk details buttons visibility`() {
        createInstance(
            riskLevelScore = RiskLevelConstants.INCREASED_RISK,
            isBackgroundJobEnabled = true,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = true,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = true,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            isBackgroundJobEnabled = true,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = true,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.INCREASED_RISK,
            isBackgroundJobEnabled = false,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = false,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = false,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            isBackgroundJobEnabled = false,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = false,
        ).apply {
            areRiskDetailsButtonsVisible(context) shouldBe true
        }
    }

    @Test
    fun `enable tracing button visibility`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            isRiskDetailsEnableTracingButtonVisible(context) shouldBe false
        }
    }

    @Test
    fun `risk details update button visibility`() {
        createInstance(
            riskLevelScore = RiskLevelConstants.INCREASED_RISK,
            isBackgroundJobEnabled = true,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = true,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = true,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            isBackgroundJobEnabled = true,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = true,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }

        createInstance(
            riskLevelScore = RiskLevelConstants.INCREASED_RISK,
            isBackgroundJobEnabled = false,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = false,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = false,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe false
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            isBackgroundJobEnabled = false,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe true
        }
        createInstance(
            riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = false,
        ).apply {
            isRiskDetailsUpdateButtonVisible(context) shouldBe true
        }
    }

    @Test
    fun `format active tracing days in retention`() {
        createInstance().apply {
            getRiskActiveTracingDaysInRetentionPeriodLogged(context)
            verify { context.getString(R.string.risk_details_information_body_period_logged_assessment) }
        }
    }
}
