package de.rki.coronawarnapp.tracing.ui.details

import android.content.Context
import android.content.res.Resources
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
        isBackgroundJobEnabled: Boolean = false
    ) = TracingDetailsState(
        tracingStatus = tracingStatus,
        riskState = riskState,
        isManualKeyRetrievalEnabled = !isBackgroundJobEnabled
    )

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
}
