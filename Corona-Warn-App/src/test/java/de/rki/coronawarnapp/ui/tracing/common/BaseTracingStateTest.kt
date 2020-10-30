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
import java.util.Date

class BaseTracingStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    val constants = listOf(
        RiskLevelConstants.UNKNOWN_RISK_INITIAL,
        RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
        RiskLevelConstants.LOW_LEVEL_RISK,
        RiskLevelConstants.INCREASED_RISK,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
        RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL,
        RiskLevelConstants.UNDETERMINED
    )

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
        override val tracingProgress: TracingProgress = tracingProgress
        override val lastRiskLevelScoreCalculated: Int = riskLevelLastSuccessfulCalculation
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
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getRiskColor(context)
            verify { context.getColor(R.color.colorSemanticNeutralRisk) }
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
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            isTracingOffRiskLevel() shouldBe false
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
        createInstance(riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
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
            context.getColor(R.color.colorStableLight)
            context.getColor(R.color.colorStableLight)
            context.getColor(R.color.colorStableLight)
        }
    }

    @Test
    fun `update button text`() {
        createInstance(manualKeyRetrievalTime = 0).apply {
            getUpdateButtonText(context)
            verify { context.getString(R.string.risk_card_button_update) }
        }
        createInstance(manualKeyRetrievalTime = 1).apply {
            getUpdateButtonText(context)
            verify { context.getString(R.string.risk_card_button_cooldown) }
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
