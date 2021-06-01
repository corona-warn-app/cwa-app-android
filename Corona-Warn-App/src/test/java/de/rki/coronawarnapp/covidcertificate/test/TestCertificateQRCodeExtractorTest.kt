package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.cryptography.AesCryptography
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import io.mockk.MockKAnnotations
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestCertificateQRCodeExtractorTest : BaseTest() {
    private val coseDecoder = HealthCertificateCOSEDecoder()
    private val headerParser = HealthCertificateHeaderParser()
    private val bodyParser = TestCertificateDccParser(Gson(), AesCryptography())

    private val extractor = TestCertificateQRCodeExtractor(coseDecoder, headerParser, bodyParser)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `happy path`() {
        //extractor.extract(TestData.qrCodeTestCertificate)
        val coseObject = TestData.coseWithEncryptedPayload.decodeBase64()!!.toByteArray()
        extractor.extract(TestData.privateKey.toByteArray(), coseObject)
    }
}
