package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CWAConfigMapperTest : BaseTest() {

    private fun createInstance() = CWAConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .addAllSupportedCountries(listOf("DE", "NL"))
            .build()
        createInstance().map(rawConfig).apply {
            this.latestVersionCode shouldBe rawConfig.latestVersionCode
            this.minVersionCode shouldBe rawConfig.minVersionCode
            this.supportedCountries shouldBe listOf("DE", "NL")
        }
    }

    @Test
    fun `invalid supported countries are filtered out`() {
        // Could happen due to protobuf scheme missmatch
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .addAllSupportedCountries(listOf("plausible deniability"))
            .build()
        createInstance().map(rawConfig).apply {
            this.latestVersionCode shouldBe rawConfig.latestVersionCode
            this.minVersionCode shouldBe rawConfig.minVersionCode
            this.supportedCountries shouldBe emptyList()
        }
    }

    @Test
    fun `if supportedCountryList is empty, we do not insert DE as fallback`() {
        // Because the UI requires this to detect when to show alternative UI elements
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            this.latestVersionCode shouldBe rawConfig.latestVersionCode
            this.minVersionCode shouldBe rawConfig.minVersionCode
            this.supportedCountries shouldBe emptyList()
        }
    }
}
