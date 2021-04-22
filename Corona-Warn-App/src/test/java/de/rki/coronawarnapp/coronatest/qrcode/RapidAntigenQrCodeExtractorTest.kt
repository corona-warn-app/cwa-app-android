package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenQrCodeExtractorTest : BaseTest() {

    private val instance = RapidAntigenQrCodeExtractor()

    @Test
    fun `valid codes are recognized`() {
        listOf(raQrCode1, raQrCode2, raQrCode3, raQrCode4, raQrCode5, raQrCode6, raQrCode7, raQrCode8).forEach {
            instance.canHandle(it) shouldBe true
        }
    }

    @Test
    fun `invalid codes are rejected`() {
        listOf(pcrQrCode1, pcrQrCode2, pcrQrCode3).forEach {
            instance.canHandle(it) shouldBe false
        }
    }

    @Test
    fun `extracting valid codes does not throw exception`() {
        listOf(raQrCode1, raQrCode2, raQrCode3, raQrCode4, raQrCode5, raQrCode6, raQrCode7, raQrCode8).forEach {
            instance.extract(it)
        }
    }

    @Test
    fun `personal data is extracted`() {
        val data = instance.extract(raQrCode3)
        data.type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        data.hash shouldBe "7b1c063e883063f8c33ffaa256aded506afd907f7446143b3da0f938a21967a9"
        data.createdAt shouldBe Instant.ofEpochMilli(1618563782000)
        data.dateOfBirth shouldBe LocalDate.parse("1962-01-08")
        data.lastName shouldBe "Hayes"
        data.firstName shouldBe "Alma"
    }

    @Test
    fun `empty strings are treated as null or notset`() {
        val data = instance.extract(raQrCodeEmptyStrings)
        data.type shouldBe CoronaTest.Type.RAPID_ANTIGEN
        data.hash shouldBe "d6e4d0181d8109bf05b346a0d2e0ef0cc472eed70d9df8c4b9ae5c7a009f3e34"
        data.createdAt shouldBe Instant.ofEpochMilli(1619012952000)
        data.dateOfBirth shouldBe null
        data.lastName shouldBe null
        data.firstName shouldBe null
    }
}
