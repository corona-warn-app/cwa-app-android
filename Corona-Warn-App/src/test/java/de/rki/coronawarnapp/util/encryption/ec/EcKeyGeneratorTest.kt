package de.rki.coronawarnapp.util.encryption.ec

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class EcKeyGeneratorTest : BaseTest() {

    @Test
    fun generateECKeyPair() {
        EcKeyGenerator().generateECKeyPair().apply {
            publicKey.algorithm shouldBe "EC"
            privateKey.algorithm shouldBe "EC"
            publicKey.encoded.size shouldBe 91
            publicKeyBase64.apply {
                length shouldBe 124
                startsWith("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE") shouldBe true
            }
        }
    }
}
