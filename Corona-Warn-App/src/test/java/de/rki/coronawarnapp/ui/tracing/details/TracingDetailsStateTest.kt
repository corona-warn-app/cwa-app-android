package de.rki.coronawarnapp.ui.tracing.details

import android.content.Context
import android.content.res.Resources
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
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
        riskState: RiskState,
        tracingProgress: TracingProgress = TracingProgress.Idle,
        matchedKeyCount: Int = 3,
        daysSinceLastExposure: Int = 2,
        activeTracingDaysInRetentionPeriod: Long = 0,
        isBackgroundJobEnabled: Boolean = false,
        isInformationBodyNoticeVisible: Boolean = false,
        isAdditionalInformationVisible: Boolean = false
    ) = TracingDetailsState(
        tracingStatus = tracingStatus,
        riskState = riskState,
        tracingProgress = tracingProgress,
        matchedKeyCount = matchedKeyCount,
        daysSinceLastExposure = daysSinceLastExposure,
        activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
        isManualKeyRetrievalEnabled = !isBackgroundJobEnabled,
        isInformationBodyNoticeVisible = isInformationBodyNoticeVisible,
        isAdditionalInformationVisible = isAdditionalInformationVisible
    )

    @Test
    fun `normal behavior visibility`() {
        createInstance(riskState = LOW_RISK).apply {
            isBehaviorNormalVisible() shouldBe true
        }
        createInstance(riskState = INCREASED_RISK).apply {
            isBehaviorNormalVisible() shouldBe false
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            isBehaviorNormalVisible() shouldBe true
        }
    }

    @Test
    fun `increased risk visibility`() {
        createInstance(riskState = LOW_RISK).apply {
            isBehaviorIncreasedRiskVisible() shouldBe false
        }
        createInstance(riskState = INCREASED_RISK).apply {
            isBehaviorIncreasedRiskVisible() shouldBe true
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            isBehaviorIncreasedRiskVisible() shouldBe false
        }
    }

    @Test
    fun `logged period card visibility`() {
        createInstance(riskState = LOW_RISK).apply {
            isBehaviorPeriodLoggedVisible() shouldBe true
        }
        createInstance(riskState = INCREASED_RISK).apply {
            isBehaviorPeriodLoggedVisible() shouldBe true
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            isBehaviorPeriodLoggedVisible() shouldBe false
        }
    }

    @Test
    fun `low level risk visibility`() {
        createInstance(riskState = LOW_RISK, matchedKeyCount = 1).apply {
            isBehaviorLowLevelRiskVisible() shouldBe true
        }
        createInstance(riskState = LOW_RISK, matchedKeyCount = 0).apply {
            isBehaviorLowLevelRiskVisible() shouldBe false
        }
        createInstance(riskState = INCREASED_RISK).apply {
            isBehaviorLowLevelRiskVisible() shouldBe false
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            isBehaviorLowLevelRiskVisible() shouldBe false
        }
    }

    @Test
    fun `risk details body text`() {
        createInstance(riskState = LOW_RISK, matchedKeyCount = 1).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_low_risk_with_encounter) }
        }
        createInstance(riskState = LOW_RISK, matchedKeyCount = 0).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_low_risk) }
        }
        createInstance(riskState = INCREASED_RISK).apply {
            getRiskDetailsRiskLevelBody(context)
            verify {
                resources.getQuantityString(
                    R.plurals.risk_details_information_body_increased_risk,
                    daysSinceLastExposure, daysSinceLastExposure
                )
            }
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskDetailsRiskLevelBody(context)
            verify { context.getString(R.string.risk_details_information_body_outdated_risk) }
        }
    }

    @Test
    fun `riskdetails body notice`() {
        createInstance(riskState = LOW_RISK).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
        createInstance(riskState = INCREASED_RISK).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice_increased) }
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskDetailsRiskLevelBodyNotice(context)
            verify { context.getString(R.string.risk_details_information_body_notice) }
        }
    }

    @Test
    fun `is tracing enable tracing button visible`() {
        createInstance(riskState = LOW_RISK, tracingStatus = GeneralTracingStatus.Status.TRACING_INACTIVE).apply {
            isRiskDetailsEnableTracingButtonVisible() shouldBe true
        }
        createInstance(riskState = LOW_RISK).apply {
            isRiskDetailsEnableTracingButtonVisible() shouldBe false
        }
        createInstance(riskState = INCREASED_RISK).apply {
            isRiskDetailsEnableTracingButtonVisible() shouldBe false
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            isRiskDetailsEnableTracingButtonVisible() shouldBe false
        }
    }

    @Test
    fun `manual update button visible`() {
        createInstance(
            riskState = INCREASED_RISK,
            isBackgroundJobEnabled = false,
            tracingStatus = GeneralTracingStatus.Status.TRACING_INACTIVE
        ).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe false
        }
        createInstance(riskState = INCREASED_RISK, isBackgroundJobEnabled = true).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe false
        }
        createInstance(riskState = INCREASED_RISK, isBackgroundJobEnabled = false).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe true
        }

        createInstance(riskState = LOW_RISK, isBackgroundJobEnabled = true).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe false
        }
        createInstance(riskState = LOW_RISK, isBackgroundJobEnabled = false).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe true
        }

        createInstance(riskState = CALCULATION_FAILED, isBackgroundJobEnabled = true).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe false
        }
        createInstance(riskState = CALCULATION_FAILED, isBackgroundJobEnabled = false).apply {
            isRiskDetailsUpdateButtonVisible() shouldBe true
        }
    }

    @Test
    fun `format active tracing days in retention`() {
        createInstance(riskState = LOW_RISK).apply {
            getRiskActiveTracingDaysInRetentionPeriodLogged(context)
            verify { context.getString(R.string.risk_details_information_body_period_logged_assessment) }
        }
    }
}
