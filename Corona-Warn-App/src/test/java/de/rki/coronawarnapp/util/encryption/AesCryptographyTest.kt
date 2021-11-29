package de.rki.coronawarnapp.util.encryption

import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AesCryptographyTest : BaseTest() {

    @Test
    fun `decrypt Hello World`() {
        val key = "d56t/juMw5r4qNx1n1igs1pobUjZBT5yq0Ct7MHUuKM=".decodeBase64()!!.toByteArray()
        val encryptedString = "WFOLewp8DWqY/8IWUHEDwg==".decodeBase64()!!.toByteArray()
        AesCryptography().decryptWithCBC(
            key,
            encryptedData = encryptedString
        ) shouldBe "Hello World".toByteArray()
    }

    @Test
    fun `encrypt Hello World`() {
        val key = "d56t/juMw5r4qNx1n1igs1pobUjZBT5yq0Ct7MHUuKM=".decodeBase64()!!.toByteArray()
        val unencryptedString = "Hello World".toByteArray()
        AesCryptography().encryptWithCBC(
            key,
            data = unencryptedString
        ) shouldBe "WFOLewp8DWqY/8IWUHEDwg==".decodeBase64()!!.toByteArray()
    }
}
