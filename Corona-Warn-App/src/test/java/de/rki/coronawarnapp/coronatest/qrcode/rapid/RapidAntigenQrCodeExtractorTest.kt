package de.rki.coronawarnapp.coronatest.qrcode.rapid

import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.pcrQrCode3
import de.rki.coronawarnapp.coronatest.qrcode.raQrAnonymousInvalidHash
import de.rki.coronawarnapp.coronatest.qrcode.raQrAnonymousValidHash
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode3
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode4
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode5
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode6
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode7
import de.rki.coronawarnapp.coronatest.qrcode.raQrIncompletePersonalData
import de.rki.coronawarnapp.coronatest.qrcode.rawPayloadWithDgcAndFullPersonalData
import de.rki.coronawarnapp.coronatest.qrcode.rawPayloadWithDgcWithoutPersonalData
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import java.time.Instant
import java.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenQrCodeExtractorTest : BaseTest() {

    private val instance = RapidAntigenQrCodeExtractor()

    @Test
    fun `valid codes are recognized`() = runTest {
        listOf(
            raQrCode1,
            raQrCode2,
            raQrCode3,
            raQrCode4,
            raQrCode5,
            raQrCode6,
            raQrCode7,
            raQrAnonymousValidHash
        ).forEach {
            instance.canHandle(it) shouldBe true
        }
    }

    @Test
    fun `invalid codes are rejected`() = runTest {
        listOf(pcrQrCode1, pcrQrCode2, pcrQrCode3).forEach {
            instance.canHandle(it) shouldBe false
        }
    }

    @Test
    fun `extracting valid codes does not throw exception`() = runTest {
        listOf(
            raQrCode1,
            raQrCode2,
            raQrCode3,
            raQrCode4,
            raQrCode5,
            raQrCode6,
            raQrCode7,
            raQrAnonymousValidHash
        ).forEach {
            instance.extract(it)
        }
    }

    @Test
    fun `personal data is extracted`() = runTest {
        val data = instance.extract(raQrCode3)
        data.type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        data.hash shouldBe "7dce08db0d4abd5ac1d2498b571afb221ca947c75c847d05466b4cfe9d95dc66"
        data.createdAt shouldBe Instant.ofEpochMilli(1619618352000)
        data.dateOfBirth shouldBe LocalDate.parse("1963-03-17")
        data.lastName shouldBe "Tyler"
        data.firstName shouldBe "Jacob"
    }

    @Test
    fun `empty strings are treated as null or not set`() = runTest {
        val data = instance.extract(raQrAnonymousValidHash)
        data.type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        data.hash shouldBe "61df099207704a072fb2a97d31687523ca50f5bb031fc58bcd325bd9a976fd68"
        data.createdAt shouldBe Instant.ofEpochMilli(1627403671000)
        data.dateOfBirth shouldBe null
        data.lastName shouldBe null
        data.firstName shouldBe null
    }

    @Test
    fun `personal data is only valid if complete or completely missing`() = runTest {
        shouldThrow<InvalidQRCodeException> { instance.extract(raQrIncompletePersonalData) }
    }

    @Test
    fun `invalid hash anonymous throws exception`() = runTest {
        shouldThrow<InvalidQRCodeException> { instance.extract(raQrAnonymousInvalidHash) }
    }

    @Test
    fun `invalid json throws exception`() = runTest {
        val invalidCode = "https://s.coronawarn.app/?v=1#eyJ0aW1lc3RhbXAiOjE2"
        shouldThrow<InvalidQRCodeException> {
            RapidAntigenQrCodeExtractor().extract(invalidCode)
        }
    }

    @Test
    fun `isDccSupportedByPoc is true only if dgc is true`() = runTest {
        val prefix = "https://s.coronawarn.app?v=1#"
        val qrCodeWithDgcAndFullPersonalData = prefix + rawPayloadWithDgcAndFullPersonalData
        val qrCodeWithDgcWithoutPersonalData = prefix + rawPayloadWithDgcWithoutPersonalData

        with(instance) {
            extract(rawString = qrCodeWithDgcAndFullPersonalData).also {
                it.isDccSupportedByPoc shouldBe true
                it.firstName shouldBe "Adelaide"
                it.lastName shouldBe "Huisman"
                it.dateOfBirth shouldBe LocalDate.parse("1959-08-02")
            }

            extract(rawString = qrCodeWithDgcWithoutPersonalData).also {
                it.isDccSupportedByPoc shouldBe true
                it.firstName shouldBe null
                it.lastName shouldBe null
                it.dateOfBirth shouldBe null
            }

            // dgc of qr is null, trust me
            extract(rawString = raQrCode1).also {
                it.isDccSupportedByPoc shouldBe false
                it.firstName shouldBe "Dylan"
                it.lastName shouldBe "Gardiner"
                it.dateOfBirth shouldBe LocalDate.parse("1967-12-22")
            }
        }
    }
}
