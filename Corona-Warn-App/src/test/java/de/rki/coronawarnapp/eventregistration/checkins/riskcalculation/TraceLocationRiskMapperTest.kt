package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest

class TraceLocationRiskMapperTest : BaseTest() {
    @MockK lateinit var configProvider: AppConfigProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = TraceLocationRiskMapper(configProvider)
}
