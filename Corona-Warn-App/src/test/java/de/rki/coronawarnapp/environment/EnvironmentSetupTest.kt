package de.rki.coronawarnapp.environment

import android.content.Context
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
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
        every { CWADebug.isDebugBuildOrMode } returns true
        val envSetup = createEnvSetup()

        EnvironmentSetup.Type.values().forEach { type ->
            envSetup.apply {
                currentEnvironment = type
                currentEnvironment shouldBe type

                downloadCdnUrl shouldBe "https://download-${type.rawKey}"
                submissionCdnUrl shouldBe "https://submission-${type.rawKey}"
                verificationCdnUrl shouldBe "https://verification-${type.rawKey}"
                appConfigVerificationKey shouldBe "12345678-${type.rawKey}"
            }
        }
    }

    @Test
    fun `default environment type is set correctly`() {
        createEnvSetup().defaultEnvironment shouldBe BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()
    }

    @Test
    fun `switching the default type is persisted in storage (preferences)`() {
        createEnvSetup().apply {
            defaultEnvironment shouldBe BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()
            currentEnvironment shouldBe defaultEnvironment
            currentEnvironment = EnvironmentSetup.Type.WRU
            currentEnvironment shouldBe EnvironmentSetup.Type.WRU
        }
        createEnvSetup().apply {
            defaultEnvironment shouldBe BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()
            currentEnvironment shouldBe EnvironmentSetup.Type.WRU
        }
    }

    @Test
    fun `test enum mapping values`() {
        EnvironmentSetup.Type.PRODUCTION.rawKey shouldBe "PROD"
        EnvironmentSetup.Type.INT.rawKey shouldBe "INT"
        EnvironmentSetup.Type.DEV.rawKey shouldBe "DEV"
        EnvironmentSetup.Type.WRU.rawKey shouldBe "WRU"
        EnvironmentSetup.Type.WRU_XA.rawKey shouldBe "WRU-XA"
        EnvironmentSetup.Type.WRU_XD.rawKey shouldBe "WRU-XD"
        EnvironmentSetup.Type.values().size shouldBe 6

        EnvironmentSetup.ENVKEY.SUBMISSION.rawKey shouldBe "SUBMISSION_CDN_URL"
        EnvironmentSetup.ENVKEY.VERIFICATION.rawKey shouldBe "VERIFICATION_CDN_URL"
        EnvironmentSetup.ENVKEY.DOWNLOAD.rawKey shouldBe "DOWNLOAD_CDN_URL"
        EnvironmentSetup.ENVKEY.VERIFICATION_KEYS.rawKey shouldBe "PUB_KEYS_SIGNATURE_VERIFICATION"
        EnvironmentSetup.ENVKEY.values().size shouldBe 4
    }

    companion object {
        private const val BAD_JSON = "{ environmentType: {\n \"SUBMISSION_CDN_U"
        private const val GOOD_JSON = """
            {
                "PROD": {
                    "SUBMISSION_CDN_URL": "https://submission-PROD",
                    "DOWNLOAD_CDN_URL": "https://download-PROD",
                    "VERIFICATION_CDN_URL": "https://verification-PROD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-PROD"
                },
                "DEV": {
                    "SUBMISSION_CDN_URL": "https://submission-DEV",
                    "DOWNLOAD_CDN_URL": "https://download-DEV",
                    "VERIFICATION_CDN_URL": "https://verification-DEV",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-DEV"
                },
                "INT": {
                    "SUBMISSION_CDN_URL": "https://submission-INT",
                    "DOWNLOAD_CDN_URL": "https://download-INT",
                    "VERIFICATION_CDN_URL": "https://verification-INT",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-INT"
                },
                "WRU": {
                    "SUBMISSION_CDN_URL": "https://submission-WRU",
                    "DOWNLOAD_CDN_URL": "https://download-WRU",
                    "VERIFICATION_CDN_URL": "https://verification-WRU",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU"
                },
                "WRU-XD": {
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XD",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XD",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XD",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XD"
                },
                "WRU-XA": {
                    "SUBMISSION_CDN_URL": "https://submission-WRU-XA",
                    "DOWNLOAD_CDN_URL": "https://download-WRU-XA",
                    "VERIFICATION_CDN_URL": "https://verification-WRU-XA",
                    "PUB_KEYS_SIGNATURE_VERIFICATION": "12345678-WRU-XA"
                }
            }
        """
    }
}
