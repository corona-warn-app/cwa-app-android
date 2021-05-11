package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.vaccination.core.certificate.DigitalCertificateCertificateV1Parser
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import de.rki.coronawarnapp.vaccination.decoder.ZLIBDecompressor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationQRCodeExtractorTest : BaseTest() {

    private val zLIBDecompressor = ZLIBDecompressor()
    private val healthCertificateCOSEDecoder = HealthCertificateCOSEDecoder()
    private val vaccinationCertificateV1Decoder = DigitalCertificateCertificateV1Parser()

    private val extractor = VaccinationQRCodeExtractor(
        zLIBDecompressor,
        healthCertificateCOSEDecoder,
        vaccinationCertificateV1Decoder
    )

    @Test
    fun `happy path extraction`() {
        extractor.extract(VaccinationQrCodeTestData.validVaccinationQrCode)
    }

    @Test
    fun `happy path extraction 2`() {
        extractor.extract(VaccinationQrCodeTestData.validVaccinationQrCode2)
    }

    @Test
    fun `valid encoding but not a health certificate fails with HC_CBOR_DECODING_FAILED`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(VaccinationQrCodeTestData.validEncoded)
        }.errorCode shouldBe HC_CBOR_DECODING_FAILED
    }

    @Test
    fun `random string fails with HC_BASE45_DECODING_FAILED`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract("nothing here to see")
        }.errorCode shouldBe HC_BASE45_DECODING_FAILED
    }

    @Test
    fun `uncompressed base45 string fails with HC_ZLIB_DECOMPRESSION_FAILED`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract("6BFOABCDEFGHIJKLMNOPQRSTUVWXYZ %*+-./:")
        }.errorCode shouldBe HC_ZLIB_DECOMPRESSION_FAILED
    }

    @Test
    fun `vaccination certificate missing fails with VC_NO_VACCINATION_ENTRY`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(VaccinationQrCodeTestData.certificateMissing)
        }.errorCode shouldBe VC_NO_VACCINATION_ENTRY
    }
}
