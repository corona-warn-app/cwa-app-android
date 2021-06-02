package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.cryptography.AesCryptography
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestCertificateQRCodeExtractorTest : BaseTest() {
    private val coseDecoder = HealthCertificateCOSEDecoder(AesCryptography())
    private val headerParser = HealthCertificateHeaderParser()
    private val bodyParser = TestCertificateDccParser(Gson())

    private val extractor = TestCertificateQRCodeExtractor(coseDecoder, headerParser, bodyParser)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `happy path qr code`() {
        extractor.extract(TestData.qrCodeTestCertificate)
    }

    @Test
    fun `happy path with cose decryption`() {
        val coseObject = TestData.coseWithEncryptedPayload.decodeBase64()!!.toByteArray()
        val dek = TestData.dek.toByteArray()
        val result = extractor.extract(dek, coseObject)
        with(result.testCertificateData.certificate.nameData) {
            familyName shouldBe "Cheng"
            givenName shouldBe "Ellen"
        }
        val result2 = extractor.extract(result.qrCode)
        with(result2.testCertificateData.certificate.nameData) {
            familyName shouldBe "Cheng"
            givenName shouldBe "Ellen"
        }
    }
}
