package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.cryptography.AesCryptography
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestCertificateQRCodeExtractorTest : BaseTest() {
    private val coseDecoder = HealthCertificateCOSEDecoder()
    private val headerParser = HealthCertificateHeaderParser()
    private val bodyParser = TestCertificateDccParser(Gson(), AesCryptography())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `happy path`() {
        TestCertificateQRCodeExtractor(coseDecoder, headerParser, bodyParser).extract(TestData.qrCodeTestCertificate)
    }
}
