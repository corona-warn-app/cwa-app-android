package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.TracingProgress
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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
        tracingStatus: Status = mockk(),
        riskState: RiskState = LOW_RISK,
        tracingProgress: TracingProgress = TracingProgress.Idle,
        isManualKeyRetrievalEnabled: Boolean = false,
        showDetails: Boolean = false
    ) = object : BaseTracingState() {
        override val tracingStatus: Status = tracingStatus
        override val riskState: RiskState = riskState
        override val tracingProgress: TracingProgress = tracingProgress
        override val showDetails: Boolean = showDetails
        override val isManualKeyRetrievalEnabled: Boolean = isManualKeyRetrievalEnabled
    }

    @Test
    fun `risk color`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticHighRisk) }
        }
        createInstance(riskState = LOW_RISK).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticLowRisk) }
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticUnknownRisk) }
        }
    }

    @Test
    fun `risk tracing off level`() {
        createInstance(riskState = CALCULATION_FAILED, tracingStatus = Status.TRACING_INACTIVE).apply {
            isTracingOff() shouldBe true
        }
        createInstance(riskState = CALCULATION_FAILED).apply {
            isTracingOff() shouldBe false
        }
        createInstance(riskState = LOW_RISK).apply {
            isTracingOff() shouldBe false
        }
        createInstance(riskState = INCREASED_RISK).apply {
            isTracingOff() shouldBe false
        }
    }

    @Test
    fun `text color`() {

        createInstance(riskState = CALCULATION_FAILED).apply {
            getStableTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        createInstance(riskState = LOW_RISK).apply {
            getStableTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1InvertedStable) }
        }
        createInstance(riskState = INCREASED_RISK).apply {
            getStableTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1InvertedStable) }
        }

        createInstance(riskState = INCREASED_RISK, tracingStatus = Status.TRACING_INACTIVE).apply {
            getStableTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
    }

    @Test
    fun `update button text`() {
        createInstance(riskState = CALCULATION_FAILED).apply {
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
