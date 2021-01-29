package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.SecureRandom
import kotlin.random.Random

class CWASafetyNetTest : BaseTest() {

    @MockK lateinit var safetyNetClientWrapper: SafetyNetClientWrapper
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var report: SafetyNetClientWrapper.Report
    @MockK lateinit var secureRandom: SecureRandom

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { environmentSetup.safetyNetApiKey } returns "very safe"
        coEvery { safetyNetClientWrapper.attest(any()) } returns report
        every { secureRandom.nextBytes(any()) } answers {
            val byteArray = arg<ByteArray>(0)
            Random(0).nextBytes(byteArray)
        }

        every { report.jwsResult } returns "JWSRESULT"
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = CWASafetyNet(
        safetyNetClientWrapper,
        secureRandom
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
            nonce shouldBe (salt + payload).toSHA256().toByteArray()
        }
    }

    @Test
    fun `attestation call generation`() = runBlockingTest {
        createInstance().apply {
            val payload = "Computer says no.".toByteArray()
            val salt = "LMK0jFCu/lOzl07ZHmtOqQ==".decodeBase64()!!

            val attestationRequest = object : DeviceAttestation.Request {
                override val scenarioPayload: ByteArray
                    get() = payload
            }
            val attestationResult = attest(attestationRequest)

            coVerify {
                safetyNetClientWrapper.attest((salt.toByteArray() + payload).toSHA256().toByteArray())
            }

            attestationResult.accessControlProtoBuf shouldBe PpacAndroid.PPACAndroid.newBuilder().apply {
                setSalt(salt.base64())
                safetyNetJws = "JWSRESULT"
            }.build()
        }
    }
}
