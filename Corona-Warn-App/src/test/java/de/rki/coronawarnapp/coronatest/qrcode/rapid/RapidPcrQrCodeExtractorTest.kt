package de.rki.coronawarnapp.coronatest.qrcode.rapid

import de.rki.coronawarnapp.coronatest.qrcode.raPcrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrAnonymousValidHash
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode1
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode2
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode3
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode4
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode5
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode6
import de.rki.coronawarnapp.coronatest.qrcode.raQrCode7
import de.rki.coronawarnapp.coronatest.qrcode.rawPayloadWithDgcAndFullPersonalData
import de.rki.coronawarnapp.coronatest.qrcode.rawPayloadWithDgcWithoutPersonalData
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidPcrQrCodeExtractorTest : BaseTest() {

    private val instance = RapidPcrQrCodeExtractor()

    private val prefix = "https://p.coronawarn.app?v=1#"
    private val qrCodeWithDgcAndFullPersonalData = prefix + rawPayloadWithDgcAndFullPersonalData
    private val qrCodeWithDgcWithoutPersonalData = prefix + rawPayloadWithDgcWithoutPersonalData

    @Test
    fun `valid codes are recognized`() = runBlockingTest {
        with(instance) {
            listOf(
                raPcrCode1,
                "https://p.coronawarn.app?v=1#ValidCode"
            ).forEach { canHandle(rawString = it) shouldBe true }
        }
    }

    @Test
    fun `invalid codes are rejected`() = runBlockingTest {
        listOf(
            raQrCode1,
            raQrCode2,
            raQrCode3,
            raQrCode4,
            raQrCode5,
            raQrCode6,
            raQrCode7,
            raQrAnonymousValidHash,
            "https://p.cOrOnAwArN.aPp?v=1#"
        ).forEach {
            instance.canHandle(it) shouldBe false
        }
    }

    @Test
    fun `creates PCR corona test qr code data with personal data`() = runBlockingTest {
        instance.extract(rawPayloadWithDgcAndFullPersonalData).also {
            it.type shouldBe BaseCoronaTest.Type.PCR
            it.createdAt shouldBe Instant.ofEpochSecond(1619618382)
            it.salt shouldBe "6EBB1C8574AF1C70BF613FB334372C72"
            it.testId shouldBe "86393115-edb0-4a7f-be85-a0b5b2693d71"
            it.hash shouldBe "32d1b9814c5e4f272795e463a2ebf29cf5d6dc7bfda83ac5bef9cd93a7b31260"
            it.firstName shouldBe "Adelaide"
            it.lastName shouldBe "Huisman"
            it.dateOfBirth shouldBe LocalDate.parse("1959-08-02")
            it.isDccSupportedByPoc shouldBe true
        }
    }

    @Test
    fun `creates PCR corona test qr code data without personal data`() = runBlockingTest {
        instance.extract(rawPayloadWithDgcWithoutPersonalData).also {
            it.type shouldBe BaseCoronaTest.Type.PCR
            it.createdAt shouldBe Instant.ofEpochSecond(1627403671)
            it.salt shouldBe "D3B5FCC2D1383474F0DE696068ABB338"
            it.testId shouldBe "730c7077-56fd-47b8-813b-fb67895a80b9"
            it.hash shouldBe "61df099207704a072fb2a97d31687523ca50f5bb031fc58bcd325bd9a976fd68"
            it.firstName shouldBe null
            it.lastName shouldBe null
            it.dateOfBirth shouldBe null
            it.isDccSupportedByPoc shouldBe true
        }
    }

    @Test
    fun `isDccSupportedByPoc is true only if dgc is true`() = runBlockingTest {
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
            extract(rawString = raPcrCode1).also {
                it.isDccSupportedByPoc shouldBe false
                it.firstName shouldBe "Isaac"
                it.lastName shouldBe "Dupuy"
                it.dateOfBirth shouldBe LocalDate.parse("1965-12-08")
            }
        }
    }
}
