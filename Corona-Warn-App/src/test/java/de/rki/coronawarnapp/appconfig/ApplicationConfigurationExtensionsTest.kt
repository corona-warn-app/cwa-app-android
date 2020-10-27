package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ApplicationConfigurationExtensionsTest : BaseTest() {

    @Test
    fun `to new Config`() {
        val orig = ApplicationConfiguration.newBuilder().addSupportedCountries("NL").build()
        orig.supportedCountriesList shouldBe listOf("NL")

        orig.toNewConfig {
            clearSupportedCountries()
            addSupportedCountries("DE")
        }.supportedCountriesList shouldBe listOf("DE")

        orig.supportedCountriesList shouldBe listOf("NL")
    }
}
