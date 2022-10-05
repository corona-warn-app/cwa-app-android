package de.rki.coronawarnapp.datadonation.safetynet

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.HashExtensions.Format.BASE64
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.gplay.GoogleApiVersion
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class CWASafetyNetTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var googleApiVersion: GoogleApiVersion
    @MockK lateinit var safetyNetClientWrapper: SafetyNetClientWrapper
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var clientReport: SafetyNetClientWrapper.Report
    @MockK lateinit var secureRandom: Random
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var cwaSettings: CWASettings

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var appConfigData: ConfigData
    @MockK lateinit var testSettings: TestSettings

    private val defaultPayload = "Computer says no.".toByteArray()
    private val firstSalt = "LMK0jFCu/lOzl07ZHmtOqQ==".decodeBase64()!!
    private val defaultNonce = (firstSalt.toByteArray() + defaultPayload).toSHA256(format = BASE64).decodeBase64()!!

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false

        every { environmentSetup.safetyNetApiKey } returns "very safe"
        coEvery { safetyNetClientWrapper.attest(any()) } returns clientReport
        every { secureRandom.nextBytes(any<ByteArray>()) } answers {
            val byteArray = arg<ByteArray>(0)
            Random(0).nextBytes(byteArray)
        }

        clientReport.apply {
            every { jwsResult } returns "JWSRESULT"
            every { nonce } returns defaultNonce
            every { apkPackageName } returns "de.rki.coronawarnapp.test"
            every { error } returns "error"
        }

        every { googleApiVersion.isPlayServicesVersionAvailable(any()) } returns true

        every { context.packageName } returns "de.rki.coronawarnapp.test"

        coEvery { appConfigProvider.getAppConfig() } returns appConfigData
        every { appConfigData.deviceTimeState } returns ConfigData.DeviceTimeState.CORRECT

        every { cwaSettings.firstReliableDeviceTime } returns flowOf(Instant.EPOCH.plus(Duration.ofDays(7)))
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofDays(8))

        every { testSettings.skipSafetyNetTimeCheck } returns flowOf(false)
    }

    private fun createInstance() = CWASafetyNet(
        context = context,
        client = safetyNetClientWrapper,
        randomSource = secureRandom,
        appConfigProvider = appConfigProvider,
        googleApiVersion = googleApiVersion,
        timeStamper = timeStamper,
        cwaSettings = cwaSettings,
        testSettings = testSettings
    )

    @Test
    fun `salt generation used injected random source`() {
        createInstance().apply {
            generateSalt() shouldBe "LMK0jFCu/lOzl07ZHmtOqQ==".decodeBase64()!!.toByteArray()
        }
    }

    @Test
    fun `nonce matches server calculation - serverstyle`() {
        // Server get's base64 encoded data and has to decode it first.
        val salt = "test-salt-1234".decodeBase64()!!.toByteArray()
        val payload = "payload-test-string".toByteArray()

        val nonce = createInstance().calculateNonce(
            salt,
            payload
        )
        nonce shouldBe "M2EqczgxveKiptESiBNRmKqxYv5raTdzyeSZyzsCvjg=".decodeBase64()
    }

    @Test
    fun `nonce matches server calculation - serverstyle - OTP Payload`() {
        // Server get's base64 encoded data and has to decode it first.
        val salt = "Ri0AXC9U+b9hE58VqupI8Q==".decodeBase64()!!.toByteArray()
        val payload = "CgtoZWxsby13b3JsZA==".decodeBase64()!!.toByteArray()

        val otp = EdusOtp.EDUSOneTimePassword.parseFrom(payload)
        otp.otp shouldBe "hello-world"

        val nonce = createInstance().calculateNonce(salt, payload)
        nonce shouldBe "ANjVoDcS8v8iQdlNrcxehSggE9WZwIp7VNpjoU7cPsg=".decodeBase64()
    }

    @Test
    fun `nonce matches server calculation - clientstyle`() {
        val payload = "Computer says no.".toByteArray()
        val salt = "Don't be so salty".toByteArray()

        val nonce = createInstance().calculateNonce(
            salt,
            payload
        )
        nonce shouldBe "Alzb6UASmHCdnnT0M8pQv5bQ/r/+lfS/jb760+ikhxc=".decodeBase64()
    }

    @Test
    fun `nonce matches server calculation - serverstyle - PPA Payload`() {
        // Server get's base64 encoded data and has to decode it first.
        val salt = "Ri0AXC9U+b9hE58VqupI8Q==".decodeBase64()!!.toByteArray()
        val payload = "Eg0IAxABGMGFyOT6LiABOgkIBBDdj6AFGAI=".decodeBase64()!!.toByteArray()

        val ppa = PpaData.PPADataAndroid.parseFrom(payload)
        ppa.exposureRiskMetadataSetList.first().riskLevel shouldBe PpaData.PPARiskLevel.RISK_LEVEL_HIGH

        val nonce = createInstance().calculateNonce(salt, payload)
        nonce shouldBe "bd6kMfLKby3pzEqW8go1ZgmHN/bU1p/4KG6+1GeB288=".decodeBase64()
    }

    @Test
    fun `nonce generation`() {
        val payload = "Computer says no.".toByteArray()
        val salt = "Don't be so salty".toByteArray()
        val nonce = createInstance().calculateNonce(salt, payload)
        nonce shouldBe (salt + payload).toSHA256(format = BASE64).decodeBase64()
    }

    @Test
    fun `successful attestation`() = runTest {
        createInstance().apply {
            val attestationResult = attest(TestAttestationRequest(defaultPayload))

            coVerify {
                safetyNetClientWrapper.attest(defaultNonce.toByteArray())
            }

            attestationResult.accessControlProtoBuf shouldBe PpacAndroid.PPACAndroid.newBuilder().apply {
                salt = firstSalt.base64()
                safetyNetJws = "JWSRESULT"
            }.build()
        }
    }

    @Test
    fun `minimum google play api version is 13000000`() = runTest {
        every { googleApiVersion.isPlayServicesVersionAvailable(any()) } returns false
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(mockk())
        }
        exception.type shouldBe SafetyNetException.Type.PLAY_SERVICES_VERSION_MISMATCH
    }

    @Test
    fun `request nonce must match response nonce`() = runTest {
        every { clientReport.nonce } returns "missmatch".decodeBase64()
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.NONCE_MISMATCH
    }

    @Test
    fun `request APK name must match response APK name`() = runTest {
        every { context.packageName } returns "package.name"

        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.APK_PACKAGE_NAME_MISMATCH
    }

    @Test
    fun `incorrect device time fails the attestation`() = runTest {
        every { appConfigData.deviceTimeState } returns ConfigData.DeviceTimeState.ASSUMED_CORRECT

        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.DEVICE_TIME_UNVERIFIED
    }

    @Test
    fun `first reliable devicetime timestamp needs to be more than 24 hours ago`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED
    }

    @Test
    fun `24h since onboarding can be skipped on deviceForTester builds`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH

        shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }.type shouldBe SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED

        every { testSettings.skipSafetyNetTimeCheck } returns flowOf(true)

        shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }.type shouldBe SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED

        every { CWADebug.isDeviceForTestersBuild } returns true

        shouldNotThrowAny {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
    }

    @Test
    fun `first reliable devicetime timestamp needs to be set`() = runTest {
        every { cwaSettings.firstReliableDeviceTime } returns flowOf(Instant.EPOCH)
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED
    }

    @Test
    fun `device time checks can be disabled via request`() = runTest {
        every { appConfigData.deviceTimeState } returns ConfigData.DeviceTimeState.ASSUMED_CORRECT
        every { timeStamper.nowUTC } returns Instant.EPOCH

        val request = TestAttestationRequest(
            "Computer says no.".toByteArray(),
            checkDeviceTime = false
        )
        createInstance().attest(request) shouldNotBe null
    }

    @Test
    fun `the request can contain a app config that should be used`() = runTest {
        val request = TestAttestationRequest(
            "Computer says no.".toByteArray(),
            configData = appConfigData
        )
        createInstance().attest(request) shouldNotBe null

        coVerify { appConfigProvider wasNot Called }
    }

    @Test
    fun `internal error triggers retry`() = runTest {
        clientReport.apply {
            every { jwsResult } returns "JWSRESULT"
            every { nonce } returns defaultNonce
            every { apkPackageName } returns "de.rki.coronawarnapp.test"
            every { error } returns "internal_error"
        }
        coEvery { safetyNetClientWrapper.attest(any()) } returns clientReport
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.INTERNAL_ERROR
    }

    data class TestAttestationRequest(
        override val scenarioPayload: ByteArray,
        override val configData: ConfigData? = null,
        override val checkDeviceTime: Boolean = true
    ) : DeviceAttestation.Request {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as TestAttestationRequest
            if (!scenarioPayload.contentEquals(other.scenarioPayload)) return false
            return true
        }

        override fun hashCode(): Int = scenarioPayload.contentHashCode()
    }
}
