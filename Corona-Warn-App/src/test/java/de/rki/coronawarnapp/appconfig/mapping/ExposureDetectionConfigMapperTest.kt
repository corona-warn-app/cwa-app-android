package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ExposureDetectionConfigMapperTest : BaseTest() {

    private fun createInstance() = ExposureDetectionConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setMinRiskScore(1)
            .build()
        createInstance().map(rawConfig).apply {
            exposureDetectionConfiguration shouldBe rawConfig.mapRiskScoreToExposureConfiguration()
            exposureDetectionParameters shouldBe rawConfig.androidExposureDetectionParameters
        }
    }
}
