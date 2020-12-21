package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass.AppFeature
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass.AppFeatures
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CWAConfigMapperTest : BaseTest() {

    private fun createInstance() = CWAConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
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
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
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
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            this.latestVersionCode shouldBe rawConfig.latestVersionCode
            this.minVersionCode shouldBe rawConfig.minVersionCode
            this.supportedCountries shouldBe emptyList()
        }
    }

    @Test
    fun `feature, device time check is disabled`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(AppFeature.newBuilder().apply {
                        label = "disable-device-time-check"
                        value = 1
                    }.build())
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe false
        }
    }

    @Test
    fun `feature, device time check is enabled`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(AppFeature.newBuilder().apply {
                        label = "disable-device-time-check"
                        value = 0
                    }.build())
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
        }
    }

    @Test
    fun `feature, device time check with unknown value`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(AppFeature.newBuilder().apply {
                        label = "disable-device-time-check"
                        value = 99
                    }.build())
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
        }
    }

    @Test
    fun `feature, device time check default value`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(AppFeature.newBuilder().build())
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
        }
    }

    @Test
    fun `feature, device time check exception`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                spyk(AppFeatures.newBuilder().build()).apply {
                    every { appFeaturesCount } throws IllegalArgumentException()
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
        }
    }
}
