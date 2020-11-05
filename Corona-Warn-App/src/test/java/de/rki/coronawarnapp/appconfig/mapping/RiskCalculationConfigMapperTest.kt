package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskCalculationConfigMapperTest : BaseTest() {

    private fun createInstance() = RiskCalculationConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            this.attenuationDuration shouldBe rawConfig.attenuationDuration
            this.minRiskScore shouldBe rawConfig.minRiskScore
            this.riskScoreClasses shouldBe rawConfig.riskScoreClasses
        }
    }
}
