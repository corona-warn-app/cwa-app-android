package de.rki.coronawarnapp.environment

import android.content.Context
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.serialization.SerializationModule
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

    private fun createEnvSetup() = EnvironmentSetup(
        context = context,
        gson = SerializationModule().baseGson()
    )

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

        EnvironmentSetup.Type.values().forEach { env ->
            envSetup.apply {
                currentEnvironment = env
                currentEnvironment shouldBe env

                useEuropeKeyPackageFiles shouldBe ENVS_WITH_EUR_PKGS.contains(env)
                downloadCdnUrl shouldBe "https://download-${env.rawKey}"
                submissionCdnUrl shouldBe "https://submission-${env.rawKey}"
                verificationCdnUrl shouldBe "https://verification-${env.rawKey}"
                appConfigVerificationKey shouldBe "12345678-${env.rawKey}"
                safetyNetApiKey shouldBe "placeholder-${env.rawKey}"
                dataDonationCdnUrl shouldBe "https://placeholder-${env.rawKey}"
            }
        }
    }

    @Test
    fun `default environment type is set correctly`() {
        createEnvSetup().defaultEnvironment shouldBe BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()
    }

    @Test
    fun `switching the default type is persisted in storage (preferences)`() {
        every { BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT } returns EnvironmentSetup.Type.DEV.rawKey
        if (CWADebug.buildFlavor == CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
            createEnvSetup().apply {
                defaultEnvironment shouldBe EnvironmentSetup.Type.DEV
                currentEnvironment shouldBe defaultEnvironment
                currentEnvironment = EnvironmentSetup.Type.WRU
                currentEnvironment shouldBe EnvironmentSetup.Type.WRU
            }
            mockPreferences.dataMapPeek.values.single() shouldBe EnvironmentSetup.Type.WRU.rawKey
            createEnvSetup().apply {
                defaultEnvironment shouldBe EnvironmentSetup.Type.DEV
                currentEnvironment shouldBe EnvironmentSetup.Type.WRU
            }
        } else {
            createEnvSetup().apply {
                defaultEnvironment shouldBe EnvironmentSetup.Type.DEV
                currentEnvironment shouldBe defaultEnvironment
                currentEnvironment = EnvironmentSetup.Type.WRU
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
        EnvironmentSetup.Type.PRODUCTION.rawKey shouldBe "PROD"
        EnvironmentSetup.Type.DEV.rawKey shouldBe "DEV"
        EnvironmentSetup.Type.INT.rawKey shouldBe "INT"
        EnvironmentSetup.Type.INT_FED.rawKey shouldBe "INT-FED"
        EnvironmentSetup.Type.WRU.rawKey shouldBe "WRU"
        EnvironmentSetup.Type.WRU_XA.rawKey shouldBe "WRU-XA"
        EnvironmentSetup.Type.WRU_XD.rawKey shouldBe "WRU-XD"
        EnvironmentSetup.Type.values().size shouldBe 7

        EnvironmentSetup.EnvKey.USE_EUR_KEY_PKGS.rawKey shouldBe "USE_EUR_KEY_PKGS"
        EnvironmentSetup.EnvKey.SUBMISSION.rawKey shouldBe "SUBMISSION_CDN_URL"
        EnvironmentSetup.EnvKey.VERIFICATION.rawKey shouldBe "VERIFICATION_CDN_URL"
        EnvironmentSetup.EnvKey.DOWNLOAD.rawKey shouldBe "DOWNLOAD_CDN_URL"
        EnvironmentSetup.EnvKey.VERIFICATION_KEYS.rawKey shouldBe "PUB_KEYS_SIGNATURE_VERIFICATION"
        EnvironmentSetup.EnvKey.DATA_DONATION.rawKey shouldBe "DATA_DONATION_CDN_URL"
        EnvironmentSetup.EnvKey.SAFETYNET_API_KEY.rawKey shouldBe "SAFETYNET_API_KEY"
        EnvironmentSetup.EnvKey.values().size shouldBe 7
    }

    companion object {
        private const val BAD_JSON = "{ environmentType: {\n \"SUBMISSION_CDN_U"
        private val ENVS_WITH_EUR_PKGS = listOf(
            EnvironmentSetup.Type.PRODUCTION,
            EnvironmentSetup.Type.INT_FED,
            EnvironmentSetup.Type.WRU_XD,
            EnvironmentSetup.Type.WRU_XA
        )
        private const val GOOD_JSON = """
            {
                "PROD": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-PROD",
                    "DOWNLOAD_CDN_URL": "https://download-PROD",
                    "VERIFICATION_CDN_URL": "https://verification-PROD",
                    "DATA_DONATION_CDN_URL": "https://placeholder-PROD",
                    "SAFETYNET_API_KEY": "placeholder-PROD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-PROD"
                },
                "DEV": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-DEV",
                    "DOWNLOAD_CDN_URL": "https://download-DEV",
                    "VERIFICATION_CDN_URL": "https://verification-DEV",
                    "DATA_DONATION_CDN_URL": "https://placeholder-DEV",
                    "SAFETYNET_API_KEY": "placeholder-DEV",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-DEV"
                },
                "INT": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-INT",
                    "DOWNLOAD_CDN_URL": "https://download-INT",
                    "VERIFICATION_CDN_URL": "https://verification-INT",
                    "DATA_DONATION_CDN_URL": "https://placeholder-INT",
                    "SAFETYNET_API_KEY": "placeholder-INT",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-INT"
                },
                "INT-FED": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-INT-FED",
                    "DOWNLOAD_CDN_URL": "https://download-INT-FED",
                    "VERIFICATION_CDN_URL": "https://verification-INT-FED",
                    "DATA_DONATION_CDN_URL": "https://placeholder-INT-FED",
                    "SAFETYNET_API_KEY": "placeholder-INT-FED",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-INT-FED"
                },
                "WRU": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-WRU",
                    "DOWNLOAD_CDN_URL": "https://download-WRU",
                    "VERIFICATION_CDN_URL": "https://verification-WRU",
                    "DATA_DONATION_CDN_URL": "https://placeholder-WRU",
                    "SAFETYNET_API_KEY": "placeholder-WRU",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU"
                },
                "WRU-XD": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XD",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XD",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XD",
                    "DATA_DONATION_CDN_URL": "https://placeholder-WRU-XD",
                    "SAFETYNET_API_KEY": "placeholder-WRU-XD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XD"
                },
                "WRU-XA": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XA",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XA",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XA",
                    "DATA_DONATION_CDN_URL": "https://placeholder-WRU-XA",
                    "SAFETYNET_API_KEY": "placeholder-WRU-XA",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XA"
                }
            }
        """
    }
}
