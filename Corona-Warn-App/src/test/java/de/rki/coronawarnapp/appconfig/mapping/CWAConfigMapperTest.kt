package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass.AppFeature
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass.AppFeatures
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CWAConfigMapperTest : BaseTest() {

    private fun createInstance() = CWAConfigMapper()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
    }

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
    fun `test boolean app config flags - should return true on 1`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "disable-device-time-check"
                value = 1
            },
            AppFeature.newBuilder().apply {
                label = "unencrypted-checkins-enabled"
                value = 1
            },
            AppFeature.newBuilder().apply {
                label = "dcc-admission-check-scenarios-enabled"
                value = 1
            },
        )

        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe false
            isUnencryptedCheckInsEnabled shouldBe true
            admissionScenariosEnabled shouldBe true
        }
    }

    @Test
    fun `test boolean app config flags - should return default value on 0`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "disable-device-time-check"
                value = 0
            },
            AppFeature.newBuilder().apply {
                label = "unencrypted-checkins-enabled"
                value = 0
            },
            AppFeature.newBuilder().apply {
                label = "dcc-admission-check-scenarios-enabled"
                value = 0
            },
        )

        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
            admissionScenariosEnabled shouldBe false
        }
    }

    @Test
    fun `test boolean app config flags should return default value on invalid integer value`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "disable-device-time-check"
                value = 2
            },
            AppFeature.newBuilder().apply {
                label = "unencrypted-checkins-enabled"
                value = 99
            },
            AppFeature.newBuilder().apply {
                label = "dcc-admission-check-scenarios-enabled"
                value = -1
            },
        )

        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
            admissionScenariosEnabled shouldBe false
        }
    }

    @Test
    fun `test boolean app config flags should return default value if flag is not available`() {
        val rawConfig = buildConfigWithAppFeatures(
            // flags not available, default values should be used
        )

        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
            admissionScenariosEnabled shouldBe false
        }
    }

    @Test
    fun `feature, dcc-person-warn-threshold`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "dcc-person-warn-threshold"
                            value = 3
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            dccPersonCountMax shouldBe 20
            dccPersonWarnThreshold shouldBe 3
        }
    }

    @Test
    fun `feature, dcc-person-warn-threshold - negative`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "dcc-person-warn-threshold"
                            value = -1
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            dccPersonCountMax shouldBe 20
            dccPersonWarnThreshold shouldBe 10
        }
    }

    @Test
    fun `feature, dcc-person-count-max`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "dcc-person-count-max"
                            value = 30
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            dccPersonCountMax shouldBe 30
            dccPersonWarnThreshold shouldBe 10
        }
    }

    @Test
    fun `feature, dcc-person-count-max - negative`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "dcc-person-count-max"
                            value = -1
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            dccPersonCountMax shouldBe 20
            dccPersonWarnThreshold shouldBe 10
        }
    }

    @Test
    fun `feature, validation-service-android-min-version-code defaults to 0`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            validationServiceMinVersion shouldBe 0
        }
    }

    @Test
    fun `feature, validation-service-android-min-version-code`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "validation-service-android-min-version-code"
                            value = 3_00_000
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            validationServiceMinVersion shouldBe 3_00_000
        }
    }

    @Test
    fun `disable-time-check-disabled feature can not be set via test settings`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "disable-device-time-check"
                            value = 0
                        }.build()
                    )
                }
            )
            .build()

        every { CWADebug.isDeviceForTestersBuild } returns false
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
        }

        every { CWADebug.isDeviceForTestersBuild } returns true
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
        }
    }

    private fun buildConfigWithAppFeatures(vararg appFeature: AppFeature.Builder) =
        ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    appFeature.forEach {
                        addAppFeatures(it.build())
                    }
                }
            ).build()
}
