package de.rki.coronawarnapp.environment

import android.content.Context
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import java.io.File

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

    private fun createEnvSetup() = EnvironmentSetup(
        context = context,
        gson = SerializationModule().baseGson()
    )

    @Test
    fun `parsing bad json throws an exception`() {
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
                appConfigPublicKey shouldBe "12345678-${env.rawKey}"
                safetyNetApiKey shouldBe "placeholder-${env.rawKey}"
                dataDonationCdnUrl shouldBe "https://datadonation-${env.rawKey}"
                logUploadServerUrl shouldBe "https://logupload-${env.rawKey}"
                crowdNotifierPublicKey shouldBe "123_abc-${env.rawKey}"
                dccServerUrl shouldBe "https://dcc-${env.rawKey}"
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
        EnvironmentSetup.Type.WRU.rawKey shouldBe "WRU"
        EnvironmentSetup.Type.WRU_XA.rawKey shouldBe "WRU-XA"
        EnvironmentSetup.Type.WRU_XD.rawKey shouldBe "WRU-XD"
        EnvironmentSetup.Type.TESTER_MOCK.rawKey shouldBe "TESTER-MOCK"
        EnvironmentSetup.Type.LOCAL.rawKey shouldBe "LOCAL"
        EnvironmentSetup.Type.values().size shouldBe 8

        EnvironmentSetup.EnvKey.USE_EUR_KEY_PKGS.rawKey shouldBe "USE_EUR_KEY_PKGS"
        EnvironmentSetup.EnvKey.SUBMISSION.rawKey shouldBe "SUBMISSION_CDN_URL"
        EnvironmentSetup.EnvKey.VERIFICATION.rawKey shouldBe "VERIFICATION_CDN_URL"
        EnvironmentSetup.EnvKey.DOWNLOAD.rawKey shouldBe "DOWNLOAD_CDN_URL"
        EnvironmentSetup.EnvKey.VERIFICATION_KEYS.rawKey shouldBe "PUB_KEYS_SIGNATURE_VERIFICATION"
        EnvironmentSetup.EnvKey.DATA_DONATION.rawKey shouldBe "DATA_DONATION_CDN_URL"
        EnvironmentSetup.EnvKey.LOG_UPLOAD.rawKey shouldBe "LOG_UPLOAD_SERVER_URL"
        EnvironmentSetup.EnvKey.SAFETYNET_API_KEY.rawKey shouldBe "SAFETYNET_API_KEY"
        EnvironmentSetup.EnvKey.CROWD_NOTIFIER_PUBLIC_KEY.rawKey shouldBe "CROWD_NOTIFIER_PUBLIC_KEY"
        EnvironmentSetup.EnvKey.DCC.rawKey shouldBe "DCC_SERVER_URL"
        EnvironmentSetup.EnvKey.values().size shouldBe 10
    }

    @Test
    fun `sanity check throws if key is missing`() {
        createEnvSetup().sanityCheck()

        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns GOOD_JSON.replace("DCC_SERVER_URL", "?")

        createEnvSetup().apply {
            val errorMessage = shouldThrow<IllegalStateException> { sanityCheck() }.message
            errorMessage shouldContain "DCC"
            errorMessage shouldContain "Failed to retrieve"
        }
    }

    @Test
    fun `enums need to be fully mapped - prod build`() {
        if (CWADebug.isDeviceForTestersBuild) return
        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns BuildConfig.ENVIRONMENT_JSONDATA

        createEnvSetup().apply {
            currentEnvironment = EnvironmentSetup.Type.PRODUCTION
            sanityCheck()
        }
    }

    @Test
    fun `enums need to be fully mapped - test build`() {
        if (!CWADebug.isDeviceForTestersBuild) return
        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns BuildConfig.ENVIRONMENT_JSONDATA

        EnvironmentSetup.Type.values().forEach { type ->
            createEnvSetup().apply {
                currentEnvironment = type
                sanityCheck()
            }
        }
    }

    @Test
    fun `actual prod env is valid`() {
        val prodEnv = File(File("").absoluteFile.parentFile, "prod_environments.json")
        require(prodEnv.exists())
        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns prodEnv.readText()

        createEnvSetup().apply {
            currentEnvironment = EnvironmentSetup.Type.PRODUCTION
            sanityCheck()
        }
    }

    @Test
    fun `actual test env is valid`() {
        val textEnv = File(File("").absoluteFile.parentFile, "test_environments.json")
        if (!textEnv.exists()) return

        every { BuildConfigWrap.ENVIRONMENT_JSONDATA } returns textEnv.readText()

        EnvironmentSetup.Type.values().forEach { type ->
            createEnvSetup().apply {
                currentEnvironment = type
                sanityCheck()
            }
        }
    }

    companion object {
        private const val BAD_JSON = "{ environmentType: {\n \"SUBMISSION_CDN_U"
        private val ENVS_WITH_EUR_PKGS = listOf(
            EnvironmentSetup.Type.PRODUCTION,
            EnvironmentSetup.Type.WRU_XD,
            EnvironmentSetup.Type.WRU_XA,
            EnvironmentSetup.Type.TESTER_MOCK,
            EnvironmentSetup.Type.LOCAL
        )
        private const val GOOD_JSON =
            """
            {
                "PROD": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-PROD",
                    "DOWNLOAD_CDN_URL": "https://download-PROD",
                    "VERIFICATION_CDN_URL": "https://verification-PROD",
                    "DATA_DONATION_CDN_URL": "https://datadonation-PROD",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-PROD",
                    "VACCINATION_CDN_URL": "https://vaccination-PROD",
                    "SAFETYNET_API_KEY": "placeholder-PROD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-PROD",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-PROD",
                    "DCC_SERVER_URL": "https://dcc-PROD"
                },
                "DEV": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-DEV",
                    "DOWNLOAD_CDN_URL": "https://download-DEV",
                    "VERIFICATION_CDN_URL": "https://verification-DEV",
                    "DATA_DONATION_CDN_URL": "https://datadonation-DEV",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-DEV",
                    "VACCINATION_CDN_URL": "https://vaccination-DEV",
                    "SAFETYNET_API_KEY": "placeholder-DEV",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-DEV",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-DEV",
                    "DCC_SERVER_URL": "https://dcc-DEV"
               },
                "INT": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-INT",
                    "DOWNLOAD_CDN_URL": "https://download-INT",
                    "VERIFICATION_CDN_URL": "https://verification-INT",
                    "DATA_DONATION_CDN_URL": "https://datadonation-INT",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-INT",
                    "VACCINATION_CDN_URL": "https://vaccination-INT",
                    "SAFETYNET_API_KEY": "placeholder-INT",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-INT",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-INT",
                    "DCC_SERVER_URL": "https://dcc-INT"
                },
                "WRU": {
                    "USE_EUR_KEY_PKGS" : false,
                    "SUBMISSION_CDN_URL": "https://submission-WRU",
                    "DOWNLOAD_CDN_URL": "https://download-WRU",
                    "VERIFICATION_CDN_URL": "https://verification-WRU",
                    "DATA_DONATION_CDN_URL": "https://datadonation-WRU",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-WRU",
                    "VACCINATION_CDN_URL": "https://vaccination-WRU",
                    "SAFETYNET_API_KEY": "placeholder-WRU",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU",
                    "CREATE_TRACELOCATION_URL": "https://tracelocation-WRU",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-WRU",
                    "DCC_SERVER_URL": "https://dcc-WRU"
                },
                "WRU-XD": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XD",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XD",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XD",
                    "DATA_DONATION_CDN_URL": "https://datadonation-WRU-XD",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-WRU-XD",
                    "VACCINATION_CDN_URL": "https://vaccination-WRU-XD",
                    "SAFETYNET_API_KEY": "placeholder-WRU-XD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XD",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-WRU-XD",
                    "DCC_SERVER_URL": "https://dcc-WRU-XD"
                },
                "WRU-XA": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XA",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XA",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XA",
                    "DATA_DONATION_CDN_URL": "https://datadonation-WRU-XA",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-WRU-XA",
                    "VACCINATION_CDN_URL": "https://vaccination-WRU-XA",
                    "SAFETYNET_API_KEY": "placeholder-WRU-XA",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XA",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-WRU-XA",
                    "DCC_SERVER_URL": "https://dcc-WRU-XA"
                },
                "TESTER-MOCK": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-TESTER-MOCK",
                    "DOWNLOAD_CDN_URL": "https://download-TESTER-MOCK",
                    "VERIFICATION_CDN_URL": "https://verification-TESTER-MOCK",
                    "DATA_DONATION_CDN_URL": "https://datadonation-TESTER-MOCK",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-TESTER-MOCK",
                    "VACCINATION_CDN_URL": "https://vaccination-TESTER-MOCK",
                    "SAFETYNET_API_KEY": "placeholder-TESTER-MOCK",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-TESTER-MOCK",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-TESTER-MOCK",
                    "DCC_SERVER_URL": "https://dcc-TESTER-MOCK"
                },
                "LOCAL": {
                    "USE_EUR_KEY_PKGS" : true,
                    "SUBMISSION_CDN_URL": "https://submission-LOCAL",
                    "DOWNLOAD_CDN_URL": "https://download-LOCAL",
                    "VERIFICATION_CDN_URL": "https://verification-LOCAL",
                    "DATA_DONATION_CDN_URL": "https://datadonation-LOCAL",
                    "LOG_UPLOAD_SERVER_URL": "https://logupload-LOCAL",
                    "VACCINATION_CDN_URL": "https://vaccination-LOCAL",
                    "SAFETYNET_API_KEY": "placeholder-LOCAL",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-LOCAL",
                    "CROWD_NOTIFIER_PUBLIC_KEY": "123_abc-LOCAL",
                    "DCC_SERVER_URL": "https://dcc-LOCAL"
                }
            }
        """
    }
}
