package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PresenceTracingRiskMapperTest : BaseTest() {
    @MockK lateinit var configProvider: AppConfigProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { configProvider.currentConfig } returns flowOf<ConfigData>()
    }

    @Test
    fun `ensure `() {
    }

    private fun createInstance() = PresenceTracingRiskMapper(configProvider)
}
