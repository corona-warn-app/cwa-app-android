package de.rki.coronawarnapp.util.encryption.rsa

import de.rki.coronawarnapp.util.encoding.base64
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RSAKeyPairGeneratorTest : BaseTest() {

    fun createInstance() = RSAKeyPairGenerator()

    @Test
    fun `default requirements are RSA 3072`() {
        RSAKeyPairGenerator.DEFAULT_MODULUS_LENGTH shouldBe 3072
        val instance = createInstance()

        instance.generate().apply {
            rawKeyPair.public.algorithm shouldBe "RSA"
            rawKeyPair.private.algorithm shouldBe "RSA"
        }
    }

    @Test
    fun `create key pair`() {
        val instance = createInstance()

        instance.generate().apply {
            rawKeyPair.public.format shouldBe "X.509"
            rawKeyPair.private.format shouldBe "PKCS#8"

            publicKey.base64 shouldBe rawKeyPair.public.encoded.base64()
            privateKey.base64 shouldBe rawKeyPair.private.encoded.base64()
        }
    }
}
