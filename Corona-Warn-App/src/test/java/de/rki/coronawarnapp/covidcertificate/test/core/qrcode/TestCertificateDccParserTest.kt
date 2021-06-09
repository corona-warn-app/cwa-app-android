package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.test.TestData
import de.rki.coronawarnapp.covidcertificate.test.core.certificate.TestDccParser
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeHex
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test

class TestCertificateDccParserTest {

    private val bodyParser = TestDccParser(Gson())

    @Test
    fun `happy path cose decryption with Ellen Cheng`() {
        val coseObject = CBORObject.DecodeFromBytes(TestData.cborObject.decodeHex().toByteArray())
        with(bodyParser.parse(coseObject)) {

            with(nameData) {
                familyName shouldBe "Musterfrau-Gößinger"
                familyNameStandardized shouldBe "MUSTERFRAU<GOESSINGER"
                givenName shouldBe "Gabriele"
                givenNameStandardized shouldBe "GABRIELE"
            }
            dob shouldBe "1998-02-26"
            dateOfBirth shouldBe LocalDate.parse("1998-02-26")
            version shouldBe "1.2.1"

            with(payloads[0]) {
                uniqueCertificateIdentifier shouldBe "URN:UVCI:01:AT:71EE2559DE38C6BF7304FB65A1A451EC#3"
                certificateCountry shouldBe "AT"
                certificateIssuer shouldBe "Ministry of Health, Austria"
                targetId shouldBe "840539006"
                sampleCollectedAt shouldBe org.joda.time.Instant.parse("2021-02-20T12:34:56+00:00")
                testType shouldBe "LP217198-3"
                testCenter shouldBe "Testing center Vienna 1"
                testNameAndManufactor shouldBe "1232"
                testResult shouldBe "260415000"
            }
        }
    }
}
