package de.rki.coronawarnapp.covidcertificate.cryptography

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AesCryptographyTest : BaseTest() {

    @Test
    fun `decrypt Hello World`() {
        val des = "d56t/juMw5r4qNx1n1igs1pobUjZBT5yq0Ct7MHUuKM=".decodeBase64()!!.toByteArray()
        val encryptedString = "WFOLewp8DWqY/8IWUHEDwg==".decodeBase64()!!.toByteArray()
        AesCryptography().decrypt(
            des,
            encryptedData = encryptedString
        ) shouldBe "Hello World".toByteArray()
    }
}
