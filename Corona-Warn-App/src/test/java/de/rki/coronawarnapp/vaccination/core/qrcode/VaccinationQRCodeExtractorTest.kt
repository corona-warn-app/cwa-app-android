package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.util.compression.ZLIBDecompressor
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1Parser
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_ISS
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationQRCodeExtractorTest : BaseTest() {

    private val zLIBDecompressor = ZLIBDecompressor()
    private val healthCertificateCOSEDecoder = HealthCertificateCOSEDecoder()
    private val headerCOSEParser = HealthCertificateHeaderParser()
    private val vaccinationDGCV1Parser = VaccinationDGCV1Parser(Gson())

    private val extractor = VaccinationQRCodeExtractor(
        zLIBDecompressor = zLIBDecompressor,
        healthCertificateCOSEDecoder = healthCertificateCOSEDecoder,
        headerParser = headerCOSEParser,
        vaccinationDGCV1Parser = vaccinationDGCV1Parser
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
    fun `happy path extraction with data`() {
        val qrCode = extractor.extract(VaccinationQrCodeTestData.validVaccinationQrCode3)

        with(qrCode.parsedData.header) {
            issuer shouldBe "AT"
            issuedAt shouldBe Instant.ofEpochSecond(1620392021)
            expiresAt shouldBe Instant.ofEpochSecond(1620564821)
        }

        with(qrCode.parsedData.vaccinationCertificate) {
            with(nameData) {
                familyName shouldBe "Musterfrau-Gößinger"
                familyNameStandardized shouldBe "MUSTERFRAU<GOESSINGER"
                givenName shouldBe "Gabriele"
                givenNameStandardized shouldBe "GABRIELE"
            }
            dob shouldBe "1998-02-26"
            dateOfBirth shouldBe LocalDate.parse("1998-02-26")
            version shouldBe "1.0.0"

            with(vaccinationDatas[0]) {
                uniqueCertificateIdentifier shouldBe "urn:uvci:01:AT:10807843F94AEE0EE5093FBC254BD813P"
                countryOfVaccination shouldBe "AT"
                doseNumber shouldBe 1
                dt shouldBe "2021-02-18"
                certificateIssuer shouldBe "BMSGPK Austria"
                marketAuthorizationHolderId shouldBe "ORG-100030215"
                medicalProductId shouldBe "EU/1/20/1528"
                totalSeriesOfDoses shouldBe 2
                targetId shouldBe "840539006"
                vaccineId shouldBe "1119305005"
                vaccinatedAt shouldBe LocalDate.parse("2021-02-18")
            }
        }
    }

    @Test
    fun `valid encoding but not a health certificate fails with VC_HC_CWT_NO_ISS`() {
        shouldThrow<InvalidHealthCertificateException> {
            extractor.extract(VaccinationQrCodeTestData.validEncoded)
        }.errorCode shouldBe VC_HC_CWT_NO_ISS
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
