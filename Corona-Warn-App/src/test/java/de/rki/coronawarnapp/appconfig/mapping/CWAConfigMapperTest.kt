package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass.AppFeature
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass.AppFeatures
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
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
    fun `feature, device time check is disabled`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "disable-device-time-check"
                            value = 1
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe false
            isUnencryptedCheckInsEnabled shouldBe false
        }
    }

    @Test
    fun `feature, device time check is enabled`() {
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
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
        }
    }

    @Test
    fun `feature, unencrypted checkins enabled`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "disable-device-time-check"
                            value = 1
                        }.build()
                    )

                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "unencrypted-checkins-enabled"
                            value = 1
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe false
            isUnencryptedCheckInsEnabled shouldBe true
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
    fun `feature, unencrypted checkins disabled`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "disable-device-time-check"
                            value = 0
                        }.build()
                    )

                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "unencrypted-checkins-enabled"
                            value = 0
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
        }
    }

    @Test
    fun `feature, unencrypted checkins disabled - value is not 1`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "disable-device-time-check"
                            value = 0
                        }.build()
                    )

                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "unencrypted-checkins-enabled"
                            value = 11
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
        }
    }

    @Test
    fun `feature, device time check with unknown value`() {
        val rawConfig = ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(
                        AppFeature.newBuilder().apply {
                            label = "disable-device-time-check"
                            value = 99
                        }.build()
                    )
                }
            )
            .build()
        createInstance().map(rawConfig).apply {
            isDeviceTimeCheckEnabled shouldBe true
            isUnencryptedCheckInsEnabled shouldBe false
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

    @Test
    fun `feature flag dcc-admission-check-scenarios-disabled is true`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "dcc-admission-check-scenarios-disabled"
                value = 1
            }
        )

        createInstance().map(rawConfig).apply {
            admissionScenariosDisabled shouldBe true
        }
    }

    @Test
    fun `feature flag dcc-admission-check-scenarios-disabled is false`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "dcc-admission-check-scenarios-disabled"
                value = 0
            }
        )

        createInstance().map(rawConfig).apply {
            admissionScenariosDisabled shouldBe false
        }
    }

    @Test
    fun `feature flag dcc-admission-check-scenarios-disabled is default value on invalid value`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "dcc-admission-check-scenarios-disabled"
                value = 99
            }
        )

        createInstance().map(rawConfig).apply {
            admissionScenariosDisabled shouldBe false
        }
    }

    @Test
    fun `feature flag dcc-admission-check-scenarios-disabled is default value if label can't be found`() {
        val rawConfig = buildConfigWithAppFeatures(
            AppFeature.newBuilder().apply {
                label = "random-label"
                value = 1
            }
        )

        createInstance().map(rawConfig).apply {
            admissionScenariosDisabled shouldBe false
        }
    }

    private fun buildConfigWithAppFeatures(appFeature: AppFeature.Builder) =
        ApplicationConfigurationAndroid.newBuilder()
            .setAppFeatures(
                AppFeatures.newBuilder().apply {
                    addAppFeatures(appFeature.build())
                }
            ).build()
}
