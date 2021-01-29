package de.rki.coronawarnapp.datadonation.safetynet

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.gplay.GoogleApiVersion
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.SecureRandom
import kotlin.random.Random

class CWASafetyNetTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var googleApiVersion: GoogleApiVersion
    @MockK lateinit var safetyNetClientWrapper: SafetyNetClientWrapper
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var clientReport: SafetyNetClientWrapper.Report
    @MockK lateinit var secureRandom: SecureRandom

    private val defaultPayload = "Computer says no.".toByteArray()
    private val firstSalt = "LMK0jFCu/lOzl07ZHmtOqQ==".decodeBase64()!!
    private val defaultNonce = (firstSalt.toByteArray() + defaultPayload).toSHA256()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { environmentSetup.safetyNetApiKey } returns "very safe"
        coEvery { safetyNetClientWrapper.attest(any()) } returns clientReport
        every { secureRandom.nextBytes(any()) } answers {
            val byteArray = arg<ByteArray>(0)
            Random(0).nextBytes(byteArray)
        }

        clientReport.apply {
            every { jwsResult } returns "JWSRESULT"
            every { nonce } returns defaultNonce
            every { apkPackageName } returns "de.rki.coronawarnapp.test"
        }

        every { googleApiVersion.isPlayServicesVersionAvailable(any()) } returns true

        every { context.packageName } returns "de.rki.coronawarnapp.test"
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = CWASafetyNet(
        context = context,
        client = safetyNetClientWrapper,
        secureRandom = secureRandom,
        appConfigProvider = appConfigProvider,
        googleApiVersion = googleApiVersion
    )

    @Test
    fun `salt generation returns new value each call`() {
        createInstance().apply {
            generateSalt() shouldBe "LMK0jFCu/lOzl07ZHmtOqQ==".decodeBase64()!!.toByteArray()
        }
    }

    @Test
    fun `nonce generation`() {
        createInstance().apply {
            val payload = "Computer says no.".toByteArray()
            val salt = "Don't be so salty".toByteArray()
            val nonce = calculateNonce(salt, payload)
            nonce shouldBe (salt + payload).toSHA256()
        }
    }

    @Test
    fun `successful attestation`() = runBlockingTest {
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
    fun `minimum google play api version is 13000000`() = runBlockingTest {
        every { googleApiVersion.isPlayServicesVersionAvailable(any()) } returns false
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(mockk())
        }
        exception.type shouldBe SafetyNetException.Type.PLAY_SERVICES_VERSION_MISMATCH
    }

    @Test
    fun `request nonce must match response nonce`() = runBlockingTest {
        every { clientReport.nonce } returns "missmatch"
        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.NONCE_MISMATCH
    }

    @Test
    fun `request APK name must match response APK name`() = runBlockingTest {
        every { context.packageName } returns "package.name"

        val exception = shouldThrow<SafetyNetException> {
            createInstance().attest(TestAttestationRequest("Computer says no.".toByteArray()))
        }
        exception.type shouldBe SafetyNetException.Type.APK_PACKAGE_NAME_MISMATCH
    }

    @Test
    fun `incorrect device time fails the attestation`() {
        TODO("DEVICE_TIME_UNVERIFIED")
    }

    @Test
    fun `first reliable devicetime timestamp needs to be more than 24 hours ago`() {
        TODO("TIME_SINCE_ONBOARDING_UNVERIFIED")
    }

    data class TestAttestationRequest(
        override val scenarioPayload: ByteArray
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
