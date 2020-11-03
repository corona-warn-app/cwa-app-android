package de.rki.coronawarnapp.ui.tracing.common

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskFormattingTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `risklevel affects icon`() {
        formatBehaviorIcon(context, RiskLevelConstants.INCREASED_RISK)
        formatBehaviorIcon(context, RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatBehaviorIcon(context, RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
        formatBehaviorIcon(context, RiskLevelConstants.LOW_LEVEL_RISK)
        formatBehaviorIcon(context, RiskLevelConstants.UNKNOWN_RISK_INITIAL)

        verifySequence {
            context.getColor(R.color.colorStableLight)
            context.getColor(R.color.colorTextSemanticNeutral)
            context.getColor(R.color.colorTextSemanticNeutral)
            context.getColor(R.color.colorStableLight)
            context.getColor(R.color.colorStableLight)
        }
    }

    @Test
    fun `risklevel affects icon background`() {
        formatBehaviorIconBackground(context, RiskLevelConstants.INCREASED_RISK)
        formatBehaviorIconBackground(context, RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatBehaviorIconBackground(
            context,
            RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF
        )
        formatBehaviorIconBackground(context, RiskLevelConstants.LOW_LEVEL_RISK)
        formatBehaviorIconBackground(context, RiskLevelConstants.UNKNOWN_RISK_INITIAL)

        verifySequence {
            context.getColor(R.color.colorSemanticHighRisk)
            context.getColor(R.color.colorSurface2)
            context.getColor(R.color.colorSurface2)
            context.getColor(R.color.colorSemanticLowRisk)
            context.getColor(R.color.colorSemanticNeutralRisk)
        }
    }
}
