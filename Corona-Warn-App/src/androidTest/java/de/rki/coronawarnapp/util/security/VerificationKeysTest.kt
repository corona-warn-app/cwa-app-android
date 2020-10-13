package de.rki.coronawarnapp.util.security

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.exception.CwaSecurityException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okio.ByteString.Companion.decodeHex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VerificationKeysTest {
    @MockK lateinit var environmentSetup: EnvironmentSetup

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { environmentSetup.appConfigVerificationKey } returns PUB_KEY
    }

    private fun createTool() = VerificationKeys(environmentSetup)

    @Test
    fun goodBinaryAndSignature() {
        val tool = createTool()
        tool.hasInvalidSignature(
            GOOD_BINARY.decodeHex().toByteArray(),
            GOOD_SIGNATURE.decodeHex().toByteArray()
        ) shouldBe false
    }

    @Test
    fun badBinaryGoodSignature() {
        val tool = createTool()
        tool.hasInvalidSignature(
            "123ABC".decodeHex().toByteArray(),
            GOOD_SIGNATURE.decodeHex().toByteArray()
        ) shouldBe true
    }

    @Test
    fun goodBinaryBadSignature() {
        val tool = createTool()
        shouldThrow<CwaSecurityException> {
            tool.hasInvalidSignature(
                GOOD_BINARY.decodeHex().toByteArray(),
                "123ABC".decodeHex().toByteArray()
            )
        }
    }

    @Test
    fun badEverything() {
        val tool = createTool()
        shouldThrow<CwaSecurityException> {
            tool.hasInvalidSignature(
                "123ABC".decodeHex().toByteArray(),
                "123ABC".decodeHex().toByteArray()
            )
        }
    }

    companion object {
        private const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg" +
                "3UVhsaDXKT0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
        private const val GOOD_BINARY =
            "080b124e0a230a034c4f57180f221a68747470733a2f2f7777772e636f726f6e617761726e2e6170700a2" +
                "70a0448494748100f188f4e221a68747470733a2f2f7777772e636f726f6e617761726e2e6170701a" +
                "600a0c1803200428053006380740081100000000000049401a0a20012801300138014001210000000" +
                "0000049402a10080510051805200528053005380540053100000000000034403a0e10021802200228" +
                "02300238024002410000000000004940221c0a040837103f121209000000000000f03f11000000000" +
                "000e03f20322a1a0a0a0a041008180212021005120c0a040801180412040801180432220a200a1c69" +
                "73506c61757369626c6544656e696162696c6974794163746976651001"
        private const val GOOD_SIGNATURE =
            "0a83010a340a1464652e726b692e636f726f6e617761726e6170701a02763122033236322a13312e322e3" +
                "834302e31303034352e342e332e32100118012247304502210099836666c962dd7a44292f6211b55e" +
                "f2364ea3a5995238c862c3b58f774237da02200ae0e793d02f92826e5dea2d7758cbfb564089b1c2a" +
                "f296d5bb80331bc5e0c5e"
    }
}
