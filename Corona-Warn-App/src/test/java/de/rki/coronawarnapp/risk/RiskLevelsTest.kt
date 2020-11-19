package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest

class RiskLevelsTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var exposureResultStore: ExposureResultStore
    private lateinit var riskLevels: DefaultRiskLevels

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { appConfigProvider.getAppConfig() } returns mockk()
        every { appConfigProvider.currentConfig } returns mockk()

        riskLevels = DefaultRiskLevels(appConfigProvider, exposureResultStore)
    }

    @Test
    fun `is within defined level threshold`() {
        riskLevels.withinDefinedLevelThreshold(2.0, 1, 3) shouldBe true
    }

    @Test
    fun `is not within defined level threshold`() {
        riskLevels.withinDefinedLevelThreshold(4.0, 1, 3) shouldBe false
    }

    @Test
    fun `is within defined level threshold - edge cases`() {
        riskLevels.withinDefinedLevelThreshold(1.0, 1, 3) shouldBe true
        riskLevels.withinDefinedLevelThreshold(3.0, 1, 3) shouldBe true
    }
}
