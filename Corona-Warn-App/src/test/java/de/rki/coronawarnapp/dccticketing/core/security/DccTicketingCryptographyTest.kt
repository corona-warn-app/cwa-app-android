package de.rki.coronawarnapp.dccticketing.core.security

import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccTicketingCryptographyTest : BaseTest() {

    val aesCryptography = DccTicketingCryptography(AesCryptography())

    @Test
    fun `encrypting Data with AES-256-CBC`() {
        aesCryptography.encryptWithCBC(
            key = "i8XlNW0rYXMDVBBsL1x+ACmA7V+EVtS2/MGRwZsTylw=".decodeBase64()!!.toByteArray(),
            data = "Hello World!",
            iv = "FWfVIhs9RGwDkqJiGsA71g=="
        ) shouldBe "dNAhkJey3d1IwO2+I9U6Ng==".decodeBase64()!!.toByteArray()

        aesCryptography.encryptWithCBC(
            key = "bZNbuUL2P2nJ3Rmb8AOWKudjpvPxlPjSM13fJNQ22yg=".decodeBase64()!!.toByteArray(),
            data = "TechSpecs are gr8!",
            iv = "2yo8Cw8MFM5xRne9bVClKg=="
        ) shouldBe "20ofpjSnTA/mkJwn8G7WvOH52cPbMtC7n8xHB7AYBzE=".decodeBase64()!!.toByteArray()
    }

    @Test
    fun `encrypting Data with AES-256-GCM`() {
        aesCryptography.encryptWithGCM(
            key = "/h2gu0ls/JTRdTPMFQ2NV/Rb4c/efLG+Y8MJ5nbOBVc=".decodeBase64()!!.toByteArray(),
            data = "Hello World!",
            iv = "zzGplm+9wbuRg2uxQdLVAg=="
        ) shouldBe "/LvIj79dTOcmXeZz7vrabu1QmbQolRyrGPdVgA==".decodeBase64()!!.toByteArray()

        aesCryptography.encryptWithGCM(
            key = "M+VfHI5c0R/A0lp63kf7nnmb0JEUppocwYUmZgFkeBc=".decodeBase64()!!.toByteArray(),
            data = "TechSpecs are gr8!",
            iv = "UVSg+ehTnE5sYaMsWai6Sw=="
        ) shouldBe "RUMOQIp8WAY9IknGrlC91ikcboYGrBD9HJd3bfUQ6zDUTA==".decodeBase64()!!.toByteArray()
    }
}
