package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

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
        tracingProgress: TracingProgress = TracingProgress.Idle,
        isManualKeyRetrievalEnabled: Boolean = false,
        showDetails: Boolean = false
    ) = object : BaseTracingState() {
        override val tracingStatus: GeneralTracingStatus.Status = tracingStatus
        override val riskLevelScore: Int = riskLevelScore
        override val tracingProgress: TracingProgress = tracingProgress
        override val showDetails: Boolean = showDetails
        override val isManualKeyRetrievalEnabled: Boolean = isManualKeyRetrievalEnabled
    }

    @Test
    fun `risk color`() {
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticHighRisk) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticUnknownRisk) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticUnknownRisk) }
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticLowRisk) }
        }
    }

    @Test
    fun `risk tracing off level`() {
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            isTracingOffRiskLevel() shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            isTracingOffRiskLevel() shouldBe true
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            isTracingOffRiskLevel() shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            isTracingOffRiskLevel() shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            isTracingOffRiskLevel() shouldBe false
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            isTracingOffRiskLevel() shouldBe false
        }
    }

    @Test
    fun `text color`() {
        createInstance(riskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getStableTextColor(context)
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getStableTextColor(context)
        }
        createInstance(riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            getStableTextColor(context)
        }
        createInstance(riskLevelScore = RiskLevelConstants.INCREASED_RISK).apply {
            getStableTextColor(context)
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            getStableTextColor(context)
        }
        createInstance(riskLevelScore = RiskLevelConstants.UNDETERMINED).apply {
            getStableTextColor(context)
        }

        verifySequence {
            context.getColor(R.color.colorTextPrimary1)
            context.getColor(R.color.colorTextPrimary1)
            context.getColor(R.color.colorStableLight)
            context.getColor(R.color.colorStableLight)
            context.getColor(R.color.colorTextPrimary1)
            context.getColor(R.color.colorTextPrimary1)
        }
    }

    @Test
    fun `update button text`() {
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET).apply {
            getUpdateButtonText(context)
            verify { context.getString(R.string.risk_card_check_failed_no_internet_restart_button) }
        }
        createInstance().apply {
            getUpdateButtonText(context)
            verify { context.getString(R.string.risk_card_button_update) }
        }
    }

    @Test
    fun `update button enabled state`() {
        createInstance(isManualKeyRetrievalEnabled = true).apply {
            isUpdateButtonEnabled() shouldBe true
        }
        createInstance(isManualKeyRetrievalEnabled = false).apply {
            isUpdateButtonEnabled() shouldBe false
        }
    }
}
