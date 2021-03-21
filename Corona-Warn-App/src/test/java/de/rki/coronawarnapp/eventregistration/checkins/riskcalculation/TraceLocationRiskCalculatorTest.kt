package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest

class TraceLocationRiskCalculatorTest : BaseTest() {

    @MockK lateinit var traceLocationRiskMapper: TraceLocationRiskMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = TraceLocationRiskCalculator(
        traceLocationRiskMapper
    )
}
