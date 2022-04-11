package de.rki.coronawarnapp.covidcertificate.revocation.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest

class RevocationHashTypeTest : BaseTest() {

    @Test
    fun `check mapping`() {
        val signatureByteString = "0a".decodeHex()
        RevocationHashType.from(signatureByteString) shouldBe RevocationHashType.SIGNATURE

        val uciByteString = "0b".decodeHex()
        RevocationHashType.from(uciByteString) shouldBe RevocationHashType.UCI

        val countryByteString = "0c".decodeHex()
        RevocationHashType.from(countryByteString) shouldBe RevocationHashType.COUNTRYCODEUCI
    }

    @Test
    fun `throws if no match`() {
        val byteString = "Invalid".decodeBase64()!!

        assertThrows<IllegalStateException> {
            RevocationHashType.from(byteString)
        }.message shouldContain byteString.hex()
    }
}
