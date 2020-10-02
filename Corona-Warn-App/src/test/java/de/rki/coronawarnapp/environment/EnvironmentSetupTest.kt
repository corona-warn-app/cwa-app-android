package de.rki.coronawarnapp.environment

import android.content.Context
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvType.Companion.toEnvironmentType
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class EnvironmentSetupTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences
    private fun createEnvSetup() = EnvironmentSetup(context)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(BuildConfigWrap)
        mockkObject(CWADebug)

        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns GOOD_JSON

        mockPreferences = MockSharedPreferences()
        every {
            context.getSharedPreferences(
                "environment_setup",
                Context.MODE_PRIVATE
            )
        } returns mockPreferences
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `parsing bad json throws an exception in debug builds`() {
        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns BAD_JSON
        shouldThrow<IllegalStateException> {
            createEnvSetup().downloadCdnUrl
        }
    }

    @Test
    fun `mapping between function and JSON variable names is correct`() {
        every { CWADebug.buildFlavor } returns CWADebug.BuildFlavor.DEVICE_FOR_TESTERS
        val envSetup = createEnvSetup()

        EnvironmentSetup.EnvType.values().forEach { env ->
            envSetup.apply {
                currentEnvironment = env
                currentEnvironment shouldBe env

                useEuropeKeyPackageFiles shouldBe ENVS_WITH_EUR_PKGS.contains(env)
                downloadCdnUrl shouldBe "https://download-${env.rawKey}"
                submissionCdnUrl shouldBe "https://submission-${env.rawKey}"
                verificationCdnUrl shouldBe "https://verification-${env.rawKey}"
                appConfigVerificationKey shouldBe "12345678-${env.rawKey}"
            }
        }
    }

    @Test
    fun `default environment type is set correctly`() {
        createEnvSetup().defaultEnvironment shouldBe BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()
    }

    @Test
    fun `switching the default type is persisted in storage (preferences)`() {
        every { BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT } returns EnvironmentSetup.EnvType.DEV.rawKey
        if (CWADebug.buildFlavor == CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
            createEnvSetup().apply {
                defaultEnvironment shouldBe EnvironmentSetup.EnvType.DEV
                currentEnvironment shouldBe defaultEnvironment
                currentEnvironment = EnvironmentSetup.EnvType.WRU
                currentEnvironment shouldBe EnvironmentSetup.EnvType.WRU
            }
            mockPreferences.dataMapPeek.values.single() shouldBe EnvironmentSetup.EnvType.WRU.rawKey
            createEnvSetup().apply {
                defaultEnvironment shouldBe EnvironmentSetup.EnvType.DEV
                currentEnvironment shouldBe EnvironmentSetup.EnvType.WRU
            }
        } else {
            createEnvSetup().apply {
                defaultEnvironment shouldBe EnvironmentSetup.EnvType.DEV
                currentEnvironment shouldBe defaultEnvironment
                currentEnvironment = EnvironmentSetup.EnvType.WRU
                currentEnvironment shouldBe defaultEnvironment
            }
            mockPreferences.dataMapPeek.values shouldBe emptyList()
            createEnvSetup().apply {
                currentEnvironment shouldBe defaultEnvironment
            }
        }
    }

    @Test
    fun `test enum mapping values`() {
        EnvironmentSetup.EnvType.PRODUCTION.rawKey shouldBe "PROD"
        EnvironmentSetup.EnvType.DEV.rawKey shouldBe "DEV"
        EnvironmentSetup.EnvType.INT.rawKey shouldBe "INT"
        EnvironmentSetup.EnvType.INT_FED.rawKey shouldBe "INT-FED"
        EnvironmentSetup.EnvType.WRU.rawKey shouldBe "WRU"
        EnvironmentSetup.EnvType.WRU_XA.rawKey shouldBe "WRU-XA"
        EnvironmentSetup.EnvType.WRU_XD.rawKey shouldBe "WRU-XD"
        EnvironmentSetup.EnvType.values().size shouldBe 7

        EnvironmentSetup.EnvKey.USE_EUR_KEY_PKGS.rawKey shouldBe "USE_EUR_KEY_PKGS"
        EnvironmentSetup.EnvKey.SUBMISSION.rawKey shouldBe "SUBMISSION_CDN_URL"
        EnvironmentSetup.EnvKey.VERIFICATION.rawKey shouldBe "VERIFICATION_CDN_URL"
        EnvironmentSetup.EnvKey.DOWNLOAD.rawKey shouldBe "DOWNLOAD_CDN_URL"
        EnvironmentSetup.EnvKey.VERIFICATION_KEYS.rawKey shouldBe "PUB_KEYS_SIGNATURE_VERIFICATION"
        EnvironmentSetup.EnvKey.values().size shouldBe 5
    }

    companion object {
        private const val BAD_JSON = "{ environmentType: {\n \"SUBMISSION_CDN_U"
        private val ENVS_WITH_EUR_PKGS = listOf(
            EnvironmentSetup.EnvType.PRODUCTION,
            EnvironmentSetup.EnvType.INT_FED,
            EnvironmentSetup.EnvType.WRU_XD,
            EnvironmentSetup.EnvType.WRU_XA
        )
        private const val GOOD_JSON = """
            {
                "PROD": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-PROD",
                    "DOWNLOAD_CDN_URL": "https://download-PROD",
                    "VERIFICATION_CDN_URL": "https://verification-PROD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-PROD"
                },
                "DEV": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-DEV",
                    "DOWNLOAD_CDN_URL": "https://download-DEV",
                    "VERIFICATION_CDN_URL": "https://verification-DEV",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-DEV"
                },
                "INT": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-INT",
                    "DOWNLOAD_CDN_URL": "https://download-INT",
                    "VERIFICATION_CDN_URL": "https://verification-INT",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-INT"
                },
                "INT-FED": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-INT-FED",
                    "DOWNLOAD_CDN_URL": "https://download-INT-FED",
                    "VERIFICATION_CDN_URL": "https://verification-INT-FED",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-INT-FED"
                },
                "WRU": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-WRU",
                    "DOWNLOAD_CDN_URL": "https://download-WRU",
                    "VERIFICATION_CDN_URL": "https://verification-WRU",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU"
                },
                "WRU-XD": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XD",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XD",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XD"
                },
                "WRU-XA": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XA",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XA",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XA",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XA"
                }
            }
        """
    }
}
