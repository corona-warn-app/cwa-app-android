package de.rki.coronawarnapp.util.encryption

import de.rki.coronawarnapp.util.encoding.base64
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CWAKeyPairGeneratorTest : BaseTest() {

    fun createInstance() = CWAKeyPairGenerator()

    @Test
    fun `default requirements are RSA 3072`() {
        val instance = createInstance()
        instance.generate().apply {
            requirements shouldBe CWAKeyPairGenerator.RSA_DEFAULT
            rawKeyPair.public.algorithm shouldBe "RSA"
            rawKeyPair.private.algorithm shouldBe "RSA"
        }

        CWAKeyPairGenerator.RSA_DEFAULT.apply {
            algorithm shouldBe CWAKeyPairGenerator.Algorithm.RSA
            modolusLength shouldBe 3072
        }
    }

    @Test
    fun `create key pair`() {
        val instance = createInstance()
        instance.generate().apply {
            requirements shouldBe CWAKeyPairGenerator.RSA_DEFAULT
            publicKey.base64 shouldBe rawKeyPair.public.encoded.base64()
            privateKey.base64 shouldBe rawKeyPair.private.encoded.base64()
        }
    }
}
