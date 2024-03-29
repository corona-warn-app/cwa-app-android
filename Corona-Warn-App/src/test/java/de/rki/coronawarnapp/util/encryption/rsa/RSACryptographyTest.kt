package de.rki.coronawarnapp.util.encryption.rsa

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class RSACryptographyTest : BaseTest() {
    private fun createInstance() = RSACryptography()

    @Test
    fun `decrypt RSA_PKCS1_OAEP`() {
        val privateKey =
            "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"
        val testData =
            "P2mcDKDZmCbCp33tW+nBNx5SGirWi213ZwOqTUpyReDvRbNPaKnpirb2RBBj2xrbTGF8nG5uZcvi8tsNIkx7GaHvJEMKzXtokvLcpiC4PJTQiO6PDacXsg02+0V/NOmg69ewSsYfLlATv/bNmn7gK+HQpc4bhnWMPOLJSoASzQQZNtSCT7Y6V7gchKYlzZsknjE4u7b3hKh4NKdWMpCLG0H5I6QhYdKPdpMd7jMCmEX2dgRTxTRQc6Ne9XR32mDUWoZ2bHUIH/jaUvxNHVDrK9f2CM7Om1U/L4cKs0MiaU4L0YH1/5K1LPfgKMucUWvR7m4F5+e6+qlES+LDGmf2VNA/g0Rgt0S5cWYFk7067uVUtZYwUXb6qZn2jK6ycTunpTBGbXccH66wPeAUObo1FWHjJwU1Hw85WA9OjxLV4HYE1heLt2wgXna5HCqflz9TmKAlxK5tcOEPRIPIsvo+mXkY4yBTJqTB+YpawZvED77BxSlsrzI+1uRPfRf63CxA"
        val expectedData = "14235414212z45676437281910293844"

        val instance = createInstance()
        val decrypted = instance.decrypt(
            toDecrypt = testData.decodeBase64()!!,
            privateKey = RSAKey.Private(privateKey.decodeBase64()!!)
        )

        decrypted.toByteArray() shouldBe expectedData.toByteArray()
    }

    @Test
    fun `encrypt and decrypt RSA_PKCS1_OAEP`() {
        val keyPair = RSAKeyPairGenerator().generate()
        val actualPlaintext = "Hello, I'm a secret!"
        val instance = createInstance()
        val encrypted = instance.encrypt(
            toEncrypt = actualPlaintext.toByteArray(),
            publicKey = keyPair.publicKey.speccedKey
        )
        val decrypted = instance.decrypt(
            toDecrypt = encrypted.toByteString(),
            privateKey = keyPair.privateKey
        ).utf8()

        decrypted shouldBe actualPlaintext
    }
}
