package de.rki.coronawarnapp.util.security

import de.rki.coronawarnapp.exception.CwaSecurityException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeHex
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VerificationKeysTest {

    private fun createTool() = VerificationKeys()

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
        private const val GOOD_BINARY =
            "080b124d0a230a034c4f57180f221a68747470733a2f2f7777772e636f726f6e617761726e2e6170700a26" +
                    "0a0448494748100f1848221a68747470733a2f2f7777772e636f726f6e617761726e2e6170701a" +
                    "640a10080110021803200428053006380740081100000000000049401a0a200128013001380140" +
                    "012100000000000049402a10080510051805200528053005380540053100000000000034403a0e" +
                    "1001180120012801300138014001410000000000004940221c0a040837103f1212090000000000" +
                    "00f03f11000000000000e03f20192a1a0a0a0a041008180212021005120c0a0408011804120408" +
                    "011804"
        private const val GOOD_SIGNATURE =
            "0a87010a380a1864652e726b692e636f726f6e617761726e6170702d6465761a02763122033236322a1331" +
                    "2e322e3834302e31303034352e342e332e321001180122473045022100cf32ff24ea18a1ffcc7f" +
                    "f4c9fe8d1808cecbc5a37e3e1d4c9ce682120450958c022064bf124b6973a9b510a43d479ff93e" +
                    "0ef97a5b893c7af4abc4a8d399969cd8a0"
    }
}
