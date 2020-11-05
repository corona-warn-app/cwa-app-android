package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CWAConfigMapperTest : BaseTest() {

    private fun createInstance() = CWAConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .addAllSupportedCountries(listOf("DE", "NL"))
            .build()
        createInstance().map(rawConfig).apply {
            this.appVersion shouldBe rawConfig.appVersion
            this.supportedCountries shouldBe listOf("DE", "NL")
        }
    }

    @Test
    fun `invalid supported countries are filtered out`() {
        // Could happen due to protobuf scheme missmatch
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .addAllSupportedCountries(listOf("plausible deniability"))
            .build()
        createInstance().map(rawConfig).apply {
            this.appVersion shouldBe rawConfig.appVersion
            this.supportedCountries shouldBe emptyList()
        }
    }

    @Test
    fun `if supportedCountryList is empty, we do not insert DE as fallback`() {
        // Because the UI requires this to detect when to show alternative UI elements
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            this.appVersion shouldBe rawConfig.appVersion
            this.supportedCountries shouldBe emptyList()
        }
    }
}
